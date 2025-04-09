# inference.py
import cv2
import numpy as np
import mediapipe as mp
from ultralytics import YOLO
import logging
from config import (
    CLASS_NUT, CLASS_FRET, NUM_FRETS, 
    MIN_SCORE_NUT, MIN_SCORE_FRET, STABLE_FRAMES, 
    REDETECT_ERROR_THRESHOLD, MAX_MISSING_FRAMES, 
    MODEL_PATH
)
from utils import (
    get_segmentation_masks,
    get_top_bottom_points,
    sort_frets_by_distance_from_nut,
    compute_initial_geometry,
    select_topmost_nut,
    match_line_corners_ordered,
    check_fret_length,
    interpolate_corners,
    measure_error_from_geometry,
    build_fretboard_polygons,
    build_string_polygons,
    find_fretboard_of_point,
    find_string_of_point_v9,
    distance,
    smooth_fret_corners,
    enforce_fret_ordering,
    get_center, 
)

# ======================================
# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Mediapipe 손가락 landmark 매핑 및 DIP landmark 매핑 (보정에 사용)
FINGER_TIP_MAP = {1: 8, 2: 12, 3: 16, 4: 20}
FINGER_DIP_MAP = {1: 7, 2: 11, 3: 15, 4: 19}
OFFSET_RATIO = 0.2

# ======================================
# 3) GuitarTracker 클래스 (세션별 상태 캡슐화)
class GuitarTracker:
    def __init__(self):
        self.model = YOLO(MODEL_PATH)
        self.hands = mp.solutions.hands.Hands(
            model_complexity=1,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        self.finger_tip_map = FINGER_TIP_MAP
        self.finger_DIP_map = FINGER_DIP_MAP
        self.reset_state()

    def reset_state(self):
        self.detection_done = False
        self.stable_count = 0
        self.fret_corners = [None]*(NUM_FRETS+1)
        self.fret_last_known = [None]*(NUM_FRETS+1)
        self.fret_geometry = [None]*(NUM_FRETS+1)
        self.init_corners = [None]*(NUM_FRETS+1)
        self.nut_box = None
        self.far_fret_box = None
        self.nut_missing_frames = 0
        self.fret_missing_frames = 0
        self.finger_positions = {}
        self.mode = "detection"

    def process_frame(self, frame: np.ndarray) -> dict:
        try:
            # 1) YOLO 추론
            results = self.model.predict(source=frame, verbose=False)[0]
            segs = get_segmentation_masks(results)
        except Exception as e:
            logger.exception("모델 추론 중 예외 발생")
            return {"detection_done": False, "finger_positions": {}}
        
        nut_candidates = []
        fret_candidates = []

        # 후보군 수집
        try:
            for mask, cid, score in segs:
                if cid == CLASS_NUT and score >= MIN_SCORE_NUT:
                    m255 = (mask * 255).astype(np.uint8)
                    m255 = cv2.resize(m255, (frame.shape[1], frame.shape[0]))
                    _, b = cv2.threshold(m255, 127, 255, cv2.THRESH_BINARY)
                    nut_candidates.append({'class_id': cid, 'bin': b})
                elif cid == CLASS_FRET and score >= MIN_SCORE_FRET:
                    m255 = (mask * 255).astype(np.uint8)
                    m255 = cv2.resize(m255, (frame.shape[1], frame.shape[0]))
                    _, b = cv2.threshold(m255, 127, 255, cv2.THRESH_BINARY)
                    fret_candidates.append({'class_id': cid, 'bin': b})
        except Exception as e:
            logger.exception("후처리(후보군 수집) 중 예외 발생")
            return {"detection_done": False, "finger_positions": {}}

        # 2) 초기 검출 모드
        if not self.detection_done:
            if len(nut_candidates) == 1 and len(fret_candidates) >= NUM_FRETS:
                self.stable_count += 1
                if self.stable_count >= STABLE_FRAMES:
                    nbin = nut_candidates[0]['bin']
                    tpt, bpt = get_top_bottom_points(nbin)
                    if tpt and bpt:
                        self.fret_corners[0] = (tpt, bpt)
                        self.fret_last_known[0] = (tpt, bpt)
                        flist = []
                        for fc in fret_candidates:
                            tfpt, bfpt = get_top_bottom_points(fc['bin'])
                            if tfpt and bfpt:
                                flist.append({'class_id': CLASS_FRET, 'lr': (tfpt, bfpt)})
                        flist = sort_frets_by_distance_from_nut((tpt, bpt), flist)
                        flist = flist[:NUM_FRETS]
                        for i, fc in enumerate(flist):
                            self.fret_corners[i+1] = fc['lr']
                            self.fret_last_known[i+1] = fc['lr']
                        for i in range(NUM_FRETS+1):
                            self.init_corners[i] = self.fret_corners[i]
                        self.fret_geometry = compute_initial_geometry(self.fret_corners)
                        self.detection_done = True
                        self.mode = "tracking"
            else:
                self.stable_count = 0
            return {"detection_done": self.detection_done, "finger_positions": {}}
        else:
            # 3) 추적 모드
            detected_list = []
            nut_bin = select_topmost_nut(nut_candidates)
            if nut_bin is not None:
                tpt, bpt = get_top_bottom_points(nut_bin)
                if tpt and bpt:
                    detected_list.append({'class_id': CLASS_NUT, 'lr': (tpt, bpt)})
            for fc in fret_candidates:
                tpt, bpt = get_top_bottom_points(fc['bin'])
                if tpt and bpt:
                    detected_list.append({'class_id': CLASS_FRET, 'lr': (tpt, bpt)})
            new_corners = match_line_corners_ordered(detected_list, self.init_corners, self.fret_geometry)
            self.fret_corners = new_corners
            self.fret_corners = check_fret_length(self.fret_corners, self.fret_last_known)
            self.fret_corners = interpolate_corners(self.fret_corners, self.fret_last_known, self.fret_geometry, self.far_fret_box)
            self.fret_corners = smooth_fret_corners(self.fret_corners)
            if self.fret_corners[0] is not None and self.fret_corners[NUM_FRETS] is not None:
                nut_center = get_center(*self.fret_corners[0])
                far_center = get_center(*self.fret_corners[NUM_FRETS])
                min_spacing = 5  
                self.fret_corners = enforce_fret_ordering(self.fret_corners, nut_center, far_center, min_spacing)
            err = measure_error_from_geometry(self.fret_corners, self.fret_geometry)
            if err > REDETECT_ERROR_THRESHOLD:
                logger.info("오차가 임계치를 초과하여 재검출 모드로 전환합니다.")
                self.reset_state()
                self.mode = "re-detection"
                return {"detection_done": False, "finger_positions": {}}
            else:
                for i in range(NUM_FRETS+1):
                    if self.fret_corners[i] is not None:
                        self.fret_last_known[i] = self.fret_corners[i]

            # 4) nut_box, far_fret_box 갱신 (string 검출을 위해)
            if len(nut_candidates) == 1:
                cnts, _ = cv2.findContours(nut_candidates[0]['bin'], cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
                if cnts:
                    cmax = max(cnts, key=cv2.contourArea)
                    self.nut_box = cv2.boundingRect(cmax)
                    self.nut_missing_frames = 0
            else:
                if self.nut_box and self.nut_missing_frames < MAX_MISSING_FRAMES:
                    self.nut_missing_frames += 1
                else:
                    self.nut_box = None
            if fret_candidates and self.nut_box:
                ncx = self.nut_box[0] + self.nut_box[2]*0.5
                ncy = self.nut_box[1] + self.nut_box[3]*0.5
                best_dist = -1
                best_box = None
                for fc in fret_candidates:
                    cnts, _ = cv2.findContours(fc['bin'], cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
                    if not cnts:
                        continue
                    cmax = max(cnts, key=cv2.contourArea)
                    fbox = cv2.boundingRect(cmax)
                    fcx = fbox[0] + fbox[2]*0.5
                    fcy = fbox[1] + fbox[3]*0.5
                    d_val = distance((ncx, ncy), (fcx, fcy))
                    if d_val > best_dist:
                        best_dist = d_val
                        best_box = fbox
                if best_box:
                    self.far_fret_box = best_box
            else:
                if self.far_fret_box and self.fret_missing_frames < MAX_MISSING_FRAMES:
                    self.fret_missing_frames += 1
                else:
                    self.far_fret_box = None
                    
            # 손가락 검출 (Mediapipe + DIP 보정)
            rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            hand_res = self.hands.process(rgb)
            self.finger_positions = {}
            fretboard_polys = build_fretboard_polygons(self.fret_corners)
            string_polys = build_string_polygons(self.nut_box, self.far_fret_box) if (self.nut_box and self.far_fret_box) else [None]*6
            if hand_res.multi_hand_landmarks and hand_res.multi_handedness:
                H, W, _ = frame.shape
                for handedness, handLms in zip(hand_res.multi_handedness, hand_res.multi_hand_landmarks):
                    if handedness.classification[0].label == "Right":
                        for finger_id in self.finger_tip_map.keys():
                            tip_idx = self.finger_tip_map[finger_id]
                            dip_idx = self.finger_DIP_map[finger_id]
                            lm_tip = handLms.landmark[tip_idx]
                            lm_dip = handLms.landmark[dip_idx]
                            tip_x = int(lm_tip.x * W)
                            tip_y = int(lm_tip.y * H)
                            dip_x = int(lm_dip.x * W)
                            dip_y = int(lm_dip.y * H)
                            vec_x = tip_x - dip_x
                            vec_y = tip_y - dip_y
                            norm = (vec_x**2 + vec_y**2) ** 0.5
                            if norm > 1e-5:
                                unit_x = vec_x / norm
                                unit_y = vec_y / norm
                                offset = int(OFFSET_RATIO * norm)
                                corrected_tip_x = tip_x + offset * unit_x
                                corrected_tip_y = tip_y + offset * unit_y
                            else:
                                corrected_tip_x, corrected_tip_y = tip_x, tip_y
                            fb_num = find_fretboard_of_point(int(corrected_tip_x), int(corrected_tip_y), fretboard_polys)
                            str_num = find_string_of_point_v9(int(corrected_tip_x), int(corrected_tip_y), string_polys)
                            self.finger_positions[finger_id] = {
                                "fretboard": fb_num,
                                "string": str_num
                            }
            return {"detection_done": self.detection_done, "finger_positions": self.finger_positions}

    def close(self):
        self.hands.close()
# 2) 유틸 함수들
import math
import cv2
import numpy as np
from config import CLASS_NUT, CLASS_FRET, NUM_FRETS

def distance(p1, p2):
    return np.linalg.norm(np.array(p1) - np.array(p2))

def angle_degrees(p0, p1):
    dx = p1[0] - p0[0]
    dy = p1[1] - p0[1]
    return np.degrees(np.arctan2(dy, dx))

def get_center(p1, p2):
    return ((p1[0] + p2[0]) * 0.5, (p1[1] + p2[1]) * 0.5)

def rotate_point(px, py, angle_deg, origin):
    angle_rad = math.radians(angle_deg)
    ox, oy = origin
    tx, ty = px - ox, py - oy
    rx = tx * math.cos(angle_rad) - ty * math.sin(angle_rad)
    ry = tx * math.sin(angle_rad) + ty * math.cos(angle_rad)
    return (rx + ox, ry + oy)

def get_segmentation_masks(results):
    if not results.masks:
        return []
    masks = results.masks.data.cpu().numpy()
    cls_ids = results.boxes.cls.cpu().numpy().astype(int)
    scores = results.boxes.conf.cpu().numpy()
    out = []
    for i in range(len(masks)):
        out.append((masks[i], cls_ids[i], scores[i]))
    return out

def get_top_bottom_points(bin_mask):
    cnts, _ = cv2.findContours(bin_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    if not cnts:
        return None, None
    max_cnt = max(cnts, key=cv2.contourArea)
    min_y, max_y = 1e9, -1e9
    top_pt, bottom_pt = None, None
    for pt in max_cnt:
        x, y = pt[0]
        if y < min_y:
            min_y = y
            top_pt = (x, y)
        if y > max_y:
            max_y = y
            bottom_pt = (x, y)
    return top_pt, bottom_pt

def select_topmost_nut(nut_candidates):
    best_nut = None
    best_top_y = None
    for nut in nut_candidates:
        result = get_top_bottom_points(nut['bin'])
        if result is None:
            continue
        top_pt, _ = result
        if best_top_y is None or top_pt[1] < best_top_y:
            best_nut = nut
            best_top_y = top_pt[1]
    return best_nut['bin'] if best_nut is not None else None

def sort_frets_by_distance_from_nut(nut_lr, fret_list):
    nut_center = get_center(*nut_lr)
    def cdist(item):
        fret_center = get_center(*item['lr'])
        return distance(nut_center, fret_center)
    fret_list.sort(key=cdist)
    return fret_list

def compute_initial_geometry(fcorners):
    geometry = [None]*(NUM_FRETS+1)
    if fcorners[0] is None:
        return geometry
    nut_center = get_center(*fcorners[0])
    for i in range(NUM_FRETS+1):
        if fcorners[i] is None:
            geometry[i] = None
            continue
        center = get_center(*fcorners[i])
        geometry[i] = {
            'dist_from_nut': distance(nut_center, center),
            'angle_from_nut': angle_degrees(nut_center, center)
        }
    return geometry

def measure_error_from_geometry(fcorners, init_geo):
    if init_geo[0] is None or fcorners[0] is None:
        return 1.0
    new_nut_center = get_center(*fcorners[0])
    ratios = []
    for i in range(1, NUM_FRETS+1):
        if fcorners[i] is None or init_geo[i] is None:
            continue
        center = get_center(*fcorners[i])
        current_dist = distance(new_nut_center, center)
        initial_dist = init_geo[i]['dist_from_nut']
        if initial_dist < 1e-5:
            continue
        ratios.append(current_dist / initial_dist)
    if len(ratios) < NUM_FRETS - 8:
        return 1.0
    avg_ratio = sum(ratios) / len(ratios)
    rel_errors = [abs(r - avg_ratio) for r in ratios]
    return sum(rel_errors) / len(rel_errors)

# def match_line_corners(detected_list, init_corners):
#     if init_corners[0] is None:
#         return [None]*(NUM_FRETS+1)
#     old_nut_center = get_center(*init_corners[0])
#     detected_nuts = [d for d in detected_list if d['class_id'] == CLASS_NUT]
#     if not detected_nuts:
#         return [None]*(NUM_FRETS+1)
#     nut_center_now = get_center(*detected_nuts[0]['lr'])
#     dx = nut_center_now[0] - old_nut_center[0]
#     dy = nut_center_now[1] - old_nut_center[1]
#     new_corners = [None]*(NUM_FRETS+1)
#     new_corners[0] = detected_nuts[0]['lr']
#     # 각도 보정 (기존 nut과 마지막 frets 비교)
#     old_far_fret = None
#     for i in range(NUM_FRETS, 0, -1):
#         if init_corners[i] is not None:
#             old_far_fret = get_center(*init_corners[i])
#             break
#     detected_frets = [d for d in detected_list if d['class_id'] == CLASS_FRET]
#     detected_fret_centers = [get_center(*d['lr']) for d in detected_frets]
#     new_far_fret = detected_fret_centers[-1] if detected_fret_centers else None
#     angle_diff = 0.0
#     if old_far_fret and new_far_fret:
#         angle_diff = angle_degrees(old_nut_center, new_far_fret) - angle_degrees(old_nut_center, old_far_fret)
#     for ditem in detected_list:
#         if ditem['class_id'] != CLASS_FRET:
#             continue
#         d_center = get_center(*ditem['lr'])
#         shifted = (d_center[0] - dx, d_center[1] - dy)
#         rotated = rotate_point(shifted[0], shifted[1], -angle_diff, old_nut_center)
#         best_i = -1
#         best_dist = float('inf')
#         for i in range(1, NUM_FRETS+1):
#             if init_corners[i] is None:
#                 continue
#             old_center = get_center(*init_corners[i])
#             d_val = distance(rotated, old_center)
#             if d_val < best_dist:
#                 best_dist = d_val
#                 best_i = i
#         if best_i >= 0:
#             new_corners[best_i] = ditem['lr']
#     return new_corners
def match_line_corners_ordered(detected_list, init_corners, fret_geometry):
    if init_corners[0] is None:
        return [None] * (NUM_FRETS + 1)
    old_nut_center = get_center(*init_corners[0])
    old_far_fret = find_last_valid_fret_center(init_corners)
    if old_far_fret is None:
        return [None] * (NUM_FRETS + 1)
    far_dist_init = distance(old_nut_center, old_far_fret)
    expected_norm = [None] * (NUM_FRETS + 1)
    for i in range(1, NUM_FRETS + 1):
        if fret_geometry[i] is not None:
            expected_norm[i] = fret_geometry[i]['dist_from_nut'] / far_dist_init
    detected_nuts = [d for d in detected_list if d['class_id'] == CLASS_NUT]
    if not detected_nuts:
        return [None] * (NUM_FRETS + 1)
    nut_center_now = get_center(*detected_nuts[0]['lr'])
    dx = nut_center_now[0] - old_nut_center[0]
    dy = nut_center_now[1] - old_nut_center[1]
    candidates = []
    for d in detected_list:
        if d['class_id'] != CLASS_FRET:
            continue
        d_center = get_center(*d['lr'])
        shifted = (d_center[0] - dx, d_center[1] - dy)
        # 간단한 회전 보정 (여기서는 0도 회전)
        rotated = rotate_point(shifted[0], shifted[1], 0, old_nut_center)
        cand_norm = distance(old_nut_center, rotated) / far_dist_init
        candidates.append((cand_norm, d['lr']))
    candidates.sort(key=lambda x: x[0])
    new_corners = [None] * (NUM_FRETS + 1)
    new_corners[0] = detected_nuts[0]['lr']
    cand_idx = 0
    THRESHOLD_NORM = 0.05
    for fret_idx in range(1, NUM_FRETS + 1):
        expected_val = expected_norm[fret_idx]
        if expected_val is None:
            continue
        assigned = None
        while cand_idx < len(candidates):
            cand_norm, cand_lr = candidates[cand_idx]
            if abs(cand_norm - expected_val) <= THRESHOLD_NORM:
                assigned = cand_lr
                cand_idx += 1
                break
            if cand_norm < expected_val:
                cand_idx += 1
            else:
                break
        new_corners[fret_idx] = assigned
    return new_corners

def smooth_fret_corners(corners_array, smooth_ratio=0.5, THRESHOLD=10):
    smoothed = corners_array.copy()
    for i in range(1, NUM_FRETS):
        if smoothed[i] is None or smoothed[i - 1] is None or smoothed[i + 1] is None:
            continue
        center_curr = np.array(get_center(*smoothed[i]))
        center_left = np.array(get_center(*smoothed[i - 1]))
        center_right = np.array(get_center(*smoothed[i + 1]))
        center_avg = (center_left + center_right) / 2
        diff = center_avg - center_curr
        error_percent = (np.linalg.norm(diff) / np.linalg.norm(center_avg)) * 100
        if error_percent > THRESHOLD:
            new_center = center_curr + smooth_ratio * diff
            top_curr, bot_curr = smoothed[i]
            offset_top = np.array(top_curr) - center_curr
            offset_bot = np.array(bot_curr) - center_curr
            new_top = new_center + offset_top
            new_bot = new_center + offset_bot
            smoothed[i] = (tuple(new_top), tuple(new_bot))
    return smoothed

def enforce_fret_ordering(fret_corners, nut_center, far_center, min_spacing):
    """
    nut_center와 far_center 사이의 방향으로 각 프렛 중심의 투영값을 계산한 후,
    각 프렛의 투영값이 최소 간격(min_spacing)만큼 증가하도록 보정.
    """
    direction = np.array(far_center) - np.array(nut_center)
    norm_dir = np.linalg.norm(direction)
    if norm_dir < 1e-5:
        return fret_corners
    unit = direction / norm_dir

    projections = []
    for fc in fret_corners:
        if fc is None:
            projections.append(None)
        else:
            center = np.array(get_center(*fc))
            proj = np.dot(center - np.array(nut_center), unit)
            projections.append(proj)

    new_corners = fret_corners.copy()
    for i in range(1, len(projections)):
        if projections[i] is None:
            continue
        prev_proj = projections[i - 1]
        if prev_proj is None:
            continue
        if projections[i] <= prev_proj + min_spacing:
            desired_proj = prev_proj + min_spacing
            diff_proj = desired_proj - projections[i]
            center = np.array(get_center(*new_corners[i]))
            new_center = center + diff_proj * unit
            top, bot = new_corners[i]
            new_top = np.array(top) + diff_proj * unit
            new_bot = np.array(bot) + diff_proj * unit
            new_corners[i] = (tuple(new_top), tuple(new_bot))
            projections[i] = desired_proj
    return new_corners

def check_fret_length(corners_array, last_known, short_factor=0.8):
    for i in range(NUM_FRETS+1):
        if corners_array[i] is None or last_known[i] is None:
            continue
        curr_len = distance(corners_array[i][0], corners_array[i][1])
        prev_len = distance(last_known[i][0], last_known[i][1])
        if prev_len > 1e-5 and curr_len < short_factor * prev_len:
            corners_array[i] = None
    return corners_array

def interpolate_corners(corners_array, last_known, fret_geometry, far_fret_box=None):
    if corners_array[0] is None:
        return corners_array
    nut_center = get_center(*corners_array[0])
    far_idx = -1
    if far_fret_box is not None:
        fx, fy, fw, fh = far_fret_box
        far_center = (fx + fw*0.5, fy + fh*0.5)
        far_idx = NUM_FRETS
    else:
        for idx in range(NUM_FRETS, 0, -1):
            if corners_array[idx] is not None:
                far_idx = idx
                break
        if far_idx == -1:
            return corners_array
        far_center = get_center(*corners_array[far_idx])
    new_total_dist = distance(nut_center, far_center)
    if fret_geometry[0] is None or fret_geometry[far_idx] is None:
        return corners_array
    far_dist_init = fret_geometry[far_idx]['dist_from_nut']
    MAX_SCALE = 1.1
    scale = new_total_dist / far_dist_init if far_dist_init > 1e-5 else 1.0
    if scale > MAX_SCALE:
        scale = MAX_SCALE
    new_total_dist = far_dist_init * scale
    for i in range(1, NUM_FRETS+1):
        if corners_array[i] is not None:
            last_known[i] = corners_array[i]
            continue
        if fret_geometry[i] is None or fret_geometry[i]['dist_from_nut'] is None:
            if last_known[i] is not None:
                corners_array[i] = last_known[i]
            continue
        ratio = fret_geometry[i]['dist_from_nut'] / fret_geometry[far_idx]['dist_from_nut']
        dist_i_now = new_total_dist * ratio
        ux = far_center[0] - nut_center[0]
        uy = far_center[1] - nut_center[1]
        base_len = math.hypot(ux, uy)
        if base_len < 1e-5:
            if last_known[i] is not None:
                corners_array[i] = last_known[i]
            continue
        ux /= base_len
        uy /= base_len
        cx = nut_center[0] + ux * dist_i_now
        cy = nut_center[1] + uy * dist_i_now
        if corners_array[far_idx] is not None:
            top_far, bot_far = corners_array[far_idx]
            seg_len = distance(top_far, bot_far)
            angle_rad = math.atan2(bot_far[1]-top_far[1], bot_far[0]-top_far[0])
            half = seg_len * 0.5
            dx = math.cos(angle_rad) * half
            dy = math.sin(angle_rad) * half
            top_pt = (cx - dx, cy - dy)
            bot_pt = (cx + dx, cy + dy)
            corners_array[i] = (top_pt, bot_pt)
        else:
            corners_array[i] = ((cx, cy - 5), (cx, cy + 5))
        last_known[i] = corners_array[i]
    return corners_array

def build_fretboard_polygons(corners_array):
    polys = [None]*NUM_FRETS
    for i in range(NUM_FRETS):
        if corners_array[i] is None or corners_array[i+1] is None:
            continue
        L1, R1 = corners_array[i]
        L2, R2 = corners_array[i+1]
        polygon = np.array([
            [int(L1[0]), int(L1[1])],
            [int(L2[0]), int(L2[1])],
            [int(R2[0]), int(R2[1])],
            [int(R1[0]), int(R1[1])]
        ], dtype=np.int32)
        polys[i] = polygon
    return polys

def find_fretboard_of_point(px, py, polygons):
    for i in range(NUM_FRETS):
        poly = polygons[i]
        if poly is None:
            continue
        res = cv2.pointPolygonTest(poly, (px, py), False)
        if res >= 0:
            return i + 1
    return None

def build_string_polygons(nut_box, far_fret_box):
    out = [None]*6
    if not nut_box or not far_fret_box:
        return out
    nx, ny, nw, nh = nut_box
    fx, fy, fw, fh = far_fret_box
    xC_n = nx + nw*0.5
    yT_n = ny
    yB_n = ny + nh
    xC_f = fx + fw*0.5
    yT_f = fy
    yB_f = fy + fh
    nut_pts = []
    fret_pts = []
    for i in range(7):
        r = i / 6
        nut_pts.append((xC_n, yT_n + (yB_n - yT_n)*r))
        fret_pts.append((xC_f, yT_f + (yB_f - yT_f)*r))
    polys = []
    for i in range(6):
        p1 = nut_pts[i]
        p2 = nut_pts[i+1]
        p3 = fret_pts[i+1]
        p4 = fret_pts[i]
        poly = np.array([p1, p2, p3, p4], dtype=np.int32)
        polys.append(poly)
    return polys

def find_string_of_point_v9(px, py, polygons):
    pt = (px, py)
    for i in range(6):
        poly = polygons[i]
        if poly is None:
            continue
        res = cv2.pointPolygonTest(poly, pt, False)
        if res >= 0:
            return 6 - i  # 보통 6번줄이 위쪽
    return None

# --- 새로 추가된 함수들 ---

def find_last_valid_fret_center(corners):
    for i in range(NUM_FRETS, 0, -1):
        if corners[i] is not None:
            return get_center(*corners[i])
    return None

def find_left_idx(corners, i):
    for idx in range(i - 1, -1, -1):
        if corners[idx] is not None:
            return idx
    return -1

def find_right_idx(corners, i):
    for idx in range(i + 1, NUM_FRETS + 1):
        if corners[idx] is not None:
            return idx
    return -1
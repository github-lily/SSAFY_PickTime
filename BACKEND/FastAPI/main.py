"""
Main application script for Guitar Chord Assistant.

This script integrates all components of the guitar chord assistant
including video-based fretboard/hand detection and audio-based chord recognition.
"""

import os
import sys
import cv2 as cv
import argparse
import logging
import time
import torch
import numpy as np
import mediapipe as mp
from datetime import datetime


# 로그 디렉토리 생성
log_dir = 'logs'
os.makedirs(log_dir, exist_ok=True)

# 현재 시간으로 로그 파일 이름 생성
current_time = datetime.now().strftime('%Y%m%d_%H%M%S')
log_file = os.path.join(log_dir, f'guitar_assistant_{current_time}.log')

# 로깅 설정: 콘솔과 파일 모두에 출력
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_file),  # 파일 핸들러
        logging.StreamHandler()         # 콘솔 핸들러
    ]
)
logger = logging.getLogger('guitar-chord-assistant')
logger.info(f"Logging to {log_file}")

# Add the project root to the path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Import project modules
from src.detection.guitar_detection import GuitarNeckDetector
import src.detection.fretboard_detection as fretboard_detection
import src.tracking.hand_tracking as hand_tracking
import src.visualization.render as render

# Import Audio modules
from src.audio.audio_pipeline import GuitarAudioPipeline
from src.audio.audio_recorder import AudioRecorder

# main.py 상단에 추가
print("Direct print test in main.py")
logger.info("Logger test in main.py")

def setup_models(guitar_model_path, string_fret_model_path=None, hand_model=None, use_gpu=True, use_audio=True):
    """
    Set up detection and tracking models.

    Args:
        guitar_model_path: Path to the trained guitar detection model
        string_fret_model_path: Path to the trained string/fret detection model (optional)
        hand_model: Optional pre-configured MediaPipe hands model
        use_gpu: Whether to use GPU if available
        use_audio: Whether to initialize audio recognition pipeline

    Returns:
        Dictionary of initialized models
    """
    models = {}

    # Choose device for PyTorch
    device = 'cuda' if use_gpu and torch.cuda.is_available() else 'cpu'
    logger.info(f"Using device: {device}")

    try:
        # Initialize guitar neck detector
        logger.info(f"Loading guitar detector from {guitar_model_path}")
        models['guitar_detector'] = GuitarNeckDetector(
            model_path=guitar_model_path,
            device=device
        )
        logger.info("Guitar neck detector initialized successfully")
    except Exception as e:
        logger.error(f"Error loading guitar detector: {str(e)}")
        raise

    # String-Fret 감지 모델 로딩 (선택적)
    if string_fret_model_path and os.path.exists(string_fret_model_path):
        try:
            from src.detection.string_fret_detector import StringFretDetector
            logger.info(f"Loading string-fret detector from {string_fret_model_path}")
            models['string_fret_detector'] = StringFretDetector(
                model_path=string_fret_model_path,
                device=device
            )
            logger.info("String-fret detector initialized successfully")
        except Exception as e:
            logger.error(f"Error loading string-fret detector: {str(e)}")
            models['string_fret_detector'] = None
    else:
        logger.info("String-fret detector not specified, will use Hough transform")
        models['string_fret_detector'] = None

    # Initialize hand tracking if not provided
    if hand_model is None:
        logger.info("Setting up MediaPipe hand tracking")
        models['hand_tracker'] = hand_tracking.setup_hand_tracking(
            static_mode=False,
            max_hands=2,
            min_detection_confidence=0.7,
            min_tracking_confidence=0.5
        )
    else:
        models['hand_tracker'] = hand_model

        # 코드 인식기 초기화 (새로운 방식)
        try:
            from src.audio.chord_recognition import GuitarChordRecognizer

            # 코드 데이터베이스 기본 경로
            chord_db_path = "models/audio/chord_database.json"
            if os.path.exists(chord_db_path):
                logger.info(f"Initializing chord recognizer with database: {chord_db_path}")
                models['chord_recognizer'] = GuitarChordRecognizer(chord_db_path=chord_db_path)
            else:
                logger.info("Chord database not found, using built-in database")
                models['chord_recognizer'] = GuitarChordRecognizer()

            logger.info(f"Chord recognizer initialized successfully")

        except ImportError as e:
            logger.error(f"Error importing GuitarChordRecognizer: {str(e)}")
            models['chord_recognizer'] = None
        except Exception as e:
            logger.error(f"Error initializing chord recognizer: {str(e)}")
            models['chord_recognizer'] = None

        # Initialize audio recognition pipeline if enabled
        if use_audio:
            try:
                logger.info("Initializing audio recognition pipeline")
                models['audio_pipeline'] = initialize_audio_pipeline()
                logger.info("Audio recognition pipeline initialized successfully")
            except Exception as e:
                logger.error(f"Error initializing audio pipeline: {str(e)}")
                models['audio_pipeline'] = None

    return models


def initialize_audio_pipeline():
    """오디오 파이프라인 초기화"""
    logger.info("오디오 파이프라인 초기화 중...")

    try:
        # CREPE 패키지가 설치되어 있는지 확인
        import crepe

        # 모델 유효성 검사 (더미 데이터로 예측)
        logger.info("CREPE 모델 테스트 중...")
        dummy_audio = np.zeros(1024, dtype=np.float32)
        crepe.predict(dummy_audio, 16000, model_capacity="tiny", step_size=1000, verbose=False)
        logger.info("CREPE 모델 초기화 완료")

        # 오디오 파이프라인 생성
        pipeline = GuitarAudioPipeline(model_capacity="tiny")
        logger.info("오디오 파이프라인 초기화 완료")

        return pipeline

    except ImportError:
        logger.error("CREPE 패키지가 설치되어 있지 않습니다. 'pip install crepe' 명령으로 설치하세요.")
        return None
    except Exception as e:
        logger.error(f"오디오 파이프라인 초기화 중 오류 발생: {e}")
        return None


def process_frame(frame, models, learning_mode=False, target_chord=None, save_debug=False):
    """
    Process a single frame with the guitar chord assistant pipeline.

    Args:
        frame: Input video frame
        models: Dictionary of initialized models
        learning_mode: Whether to operate in learning mode (with target chord)
        target_chord: Optional target chord for learning mode
        save_debug: Whether to save debug images

    Returns:
        Dictionary with processing results and visualization
    """
    # Initialize result dictionary
    result = {
        'success': False,
        'neck_detected': False,
        'fretboard_detected': False,
        'hand_detected': False,
        'chord_recognized': False,
        'visualization': None,
        'timestamp': time.time()
    }

    # Create copy for visualization
    vis_frame = frame.copy()
    result['visualization'] = vis_frame  # 초기값 설정

    try:
        # logger.info("------- New Frame Processing -------")
        # Step 1: Detect guitar neck
        # logger.info("Step 1: Detecting guitar neck")
        detections = models['guitar_detector'].detect(frame)

        # Check if any necks detected
        if not detections['crops']:
            # logger.info("No guitar neck detected")
            if save_debug:
                vis_frame = render.create_status_bar(
                    vis_frame, "No guitar neck detected", progress=0)
                result['visualization'] = vis_frame
            return result

        # Update result with neck detection
        result['neck_detected'] = True
        # logger.info(f"Guitar neck detected! Position: {detections['crops'][0]['position']}")

        # 넥 영역 추출 후
        neck_crop = detections['crops'][0]['image']
        neck_position = detections['crops'][0]['position']
        x1, y1, x2, y2 = neck_position  # 넥 영역 위치 저장

        # 여기를 수정: YOLO 모델이 있으면 사용, 없으면 기존 방식 사용
        if 'string_fret_detector' in models and models['string_fret_detector'] is not None:
            # YOLO 모델로 string과 fret 감지
            logger.info("Using YOLO model for string/fret detection")
            detection_result = models['string_fret_detector'].detect(neck_crop, save_debug=save_debug)
            strings = detection_result['strings']
            frets = detection_result['frets']
            # raw_detections 전달
            raw_detections = detection_result.get('raw_detections')
        else:
            # 기존 Hough 변환 방식으로 string과 fret 감지
            logger.info("Using Hough transform for string/fret detection")
            lines = fretboard_detection.detect_strings_and_frets(
                neck_crop,
                string_threshold=500,
                fret_threshold=250,
                verbose=True,
                save_debug=save_debug
            )
            strings = lines['strings']
            frets = lines['frets']
            raw_detections = None

        # Step 3: Create virtual fretboard
        # logger.info("Step 3: Creating virtual fretboard")
        try:
            print("DIRECT DEBUG: Before creating virtual fretboard")
            print(f"DIRECT DEBUG: strings type = {type(strings)}, frets type = {type(frets)}")

            # 넥 방향 분석
            string_angle, fret_angle = fretboard_detection.analyze_neck_orientation(
                neck_crop, neck_position
            )

            # 성능 최적화를 위한 각도 평활화 (선택사항)
            if not hasattr(result, 'prev_string_angle'):
                result['prev_string_angle'] = string_angle
                result['prev_fret_angle'] = fret_angle
            else:
                # 지수 이동 평균 적용
                alpha = 0.3  # 평활화 계수 (0.1-0.5 권장)
                string_angle = alpha * string_angle + (1 - alpha) * result['prev_string_angle']
                fret_angle = alpha * fret_angle + (1 - alpha) * result['prev_fret_angle']

                result['prev_string_angle'] = string_angle
                result['prev_fret_angle'] = fret_angle

            fretboard = fretboard_detection.create_virtual_fretboard(
                strings, frets, neck_crop.shape,
                raw_detections=raw_detections,
                string_angle=string_angle,  # 분석된 각도 전달
                fret_angle=fret_angle,  # 분석된 각도 전달
                save_debug=save_debug
            )

            print("DIRECT DEBUG: After creating virtual fretboard")
            print(f"DIRECT DEBUG: fretboard keys = {fretboard.keys()}")

            # 디버그 이미지 생성 (항상)
            import cv2 as cv
            import os
            import numpy as np

            temp_dir = "temp"
            os.makedirs(temp_dir, exist_ok=True)

            debug_img = neck_crop.copy()
            h, w = debug_img.shape[:2]

            # 대각선 그리기
            cv.line(debug_img, (0, 0), (w, h), (0, 255, 255), 3)

            cv.imwrite(os.path.join(temp_dir, 'main_debug.jpg'), debug_img)
            print("DIRECT DEBUG: Saved main_debug.jpg")

            # Check if valid fretboard
            # if not fretboard_detection.is_valid_fretboard(fretboard):
            #     # logger.info("Invalid fretboard model created")
            #     if save_debug:
            #         vis_frame = render.create_status_bar(
            #             vis_frame, "Invalid fretboard model", progress=35)
            #         result['visualization'] = vis_frame
            #     return result

            # Log fretboard details
            # logger.info(
            #     f"Valid fretboard created with {len(fretboard['string_positions'])} strings and {len(fretboard['fret_positions'])} frets")

        except Exception as e:
            # logger.error(f"Error in fretboard creation: {str(e)}")
            if save_debug:
                vis_frame = render.create_status_bar(
                    vis_frame, f"Fretboard creation error: {str(e)}", progress=35)
                result['visualization'] = vis_frame
            return result

        # Update result with fretboard detection
        result['fretboard_detected'] = True
        result['fretboard'] = fretboard

        # Step 4: Track hand with MediaPipe
        # logger.info("Step 4: Tracking hands")
        try:
            hand_results = hand_tracking.track_hand(
                frame,
                models['hand_tracker'],
                save_debug=save_debug
            )

            # Check if hands detected
            if hand_results is None:
                print("DIRECT DEBUG: No hands detected, using fretboard visualization")
                try:
                    # 실제 기울어진 선 구현
                    import cv2 as cv
                    import numpy as np

                    # 기울어진 선 시각화
                    vis_neck = neck_crop.copy()
                    height, width = vis_neck.shape[:2]

                    # 엔드포인트를 사용하여 기울어진 선 그리기
                    if 'string_endpoints' in fretboard and fretboard['string_endpoints']:
                        for p1, p2 in fretboard['string_endpoints']:
                            try:
                                cv.line(vis_neck, p1, p2, (0, 255, 0), 1)  # 녹색 - 문자열
                            except Exception as e:
                                logger.error(f"Error drawing string: {e}")

                    if 'fret_endpoints' in fretboard and fretboard['fret_endpoints']:
                        for p1, p2 in fretboard['fret_endpoints']:
                            try:
                                cv.line(vis_neck, p1, p2, (0, 0, 255), 1)  # 빨강 - 프렛
                            except Exception as e:
                                logger.error(f"Error drawing fret: {e}")

                    # 각도 정보 표시 (선택사항)
                    string_angle = fretboard.get('string_angle', 0)
                    fret_angle = fretboard.get('fret_angle', 90)
                    cv.putText(vis_neck, f"S: {string_angle:.1f}°", (10, 20),
                               cv.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)
                    cv.putText(vis_neck, f"F: {fret_angle:.1f}°", (10, 40),
                               cv.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1)
                except Exception as e:
                    logger.error(f"Error in fretboard visualization: {e}")
                    # 오류 발생 시 기존 방식으로 대체
                    vis_neck = fretboard_detection.visualize_fretboard(neck_crop, fretboard)

                vis_frame[y1:y2, x1:x2] = vis_neck

                # if save_debug:
                #     vis_frame = render.create_status_bar(
                #         vis_frame, "No hands detected", progress=50)
                #     result['visualization'] = vis_frame
                return result

            # Log hand detection result
            # logger.info("Hand(s) detected successfully")

        except Exception as e:
            # logger.error(f"Error in hand tracking: {str(e)}")
            # We can still visualize the fretboard
            try:
                if 'string_fret_detector' in models and models['string_fret_detector'] is not None:
                    # YOLO 경로용 시각화 함수 사용
                    vis_neck = models['string_fret_detector'].visualize_fretboard(neck_crop, fretboard, angles=True)
                else:
                    # 기존 방식 사용
                    vis_neck = fretboard_detection.visualize_fretboard(neck_crop, fretboard)
                vis_frame[y1:y2, x1:x2] = vis_neck
            except:
                pass

            if save_debug:
                vis_frame = render.create_status_bar(
                    vis_frame, f"Hand tracking error: {str(e)}", progress=50)
                result['visualization'] = vis_frame
            return result

        # Update result with hand detection
        result['hand_detected'] = True
        result['hand_results'] = hand_results

        # Step 5: Map hand landmarks to fretboard
        # logger.info("Step 5: Mapping finger positions to fretboard")
        try:
            finger_mappings = hand_tracking.map_hand_to_fretboard(
                hand_results, fretboard, frame.shape)

            # Check if any fingers mapped to fretboard
            if not finger_mappings:
                # logger.info("No fingers mapped to fretboard")
                # No fingers on fretboard
                if 'string_fret_detector' in models and models['string_fret_detector'] is not None:
                    # YOLO 경로용 시각화 함수 사용
                    vis_neck = models['string_fret_detector'].visualize_fretboard(neck_crop, fretboard, angles=True)
                else:
                    # 기존 방식 사용
                    vis_neck = fretboard_detection.visualize_fretboard(neck_crop, fretboard)
                vis_frame[y1:y2, x1:x2] = vis_neck

                # Draw hand landmarks anyway if available
                if isinstance(hand_results, dict) and 'landmarks' in hand_results:
                    landmarks_list = hand_results['landmarks']
                elif isinstance(hand_results, list):
                    landmarks_list = hand_results
                else:
                    landmarks_list = [hand_results] if hand_results is not None else []

                for landmarks in landmarks_list:
                    mp_drawing = mp.solutions.drawing_utils
                    mp_drawing.draw_landmarks(
                        vis_frame,
                        landmarks,
                        mp.solutions.hands.HAND_CONNECTIONS
                    )

                if save_debug:
                    vis_frame = render.create_status_bar(
                        vis_frame, "No fingers detected on fretboard", progress=65)
                    result['visualization'] = vis_frame
                return result

            # Log finger mapping results
            # logger.info(f"Successfully mapped {len(finger_mappings)} fingers to fretboard")
            for finger, data in finger_mappings.items():
                if isinstance(data, dict):
                    logger.info(
                        f"  {finger}: string {data.get('string', 'N/A')}, fret {data.get('fret', 'N/A')}, prob {data.get('probability', 0):.2f}")
                else:
                    logger.info(f"  {finger}: {data}")

        except Exception as e:
            # logger.error(f"Error in finger mapping: {str(e)}")
            # Visualize whatever we can
            try:
                if 'string_fret_detector' in models and models['string_fret_detector'] is not None:
                    # YOLO 경로용 시각화 함수 사용
                    vis_neck = models['string_fret_detector'].visualize_fretboard(neck_crop, fretboard, angles=True)
                else:
                    # 기존 방식 사용
                    vis_neck = fretboard_detection.visualize_fretboard(neck_crop, fretboard)
                vis_frame[y1:y2, x1:x2] = vis_neck

                # Draw hand landmarks if available
                if isinstance(hand_results, dict) and 'landmarks' in hand_results:
                    landmarks_list = hand_results['landmarks']
                    for landmarks in landmarks_list:
                        mp_drawing = mp.solutions.drawing_utils
                        mp_drawing.draw_landmarks(
                            vis_frame,
                            landmarks,
                            mp.solutions.hands.HAND_CONNECTIONS
                        )
            except:
                pass

            if save_debug:
                vis_frame = render.create_status_bar(
                    vis_frame, f"Finger mapping error: {str(e)}", progress=65)
                result['visualization'] = vis_frame
            return result

        # Update result with finger mappings
        result['finger_mappings'] = finger_mappings

        # Step 6: Chord recognition
        # logger.info("Step 6: Recognizing chord")
        try:
            if learning_mode and target_chord:
                # Learning mode: compare with target chord
                from utils.chord_recognition import compare_with_target_chord

                comparison = compare_with_target_chord(finger_mappings, target_chord)

                chord_info = {
                    'chord_name': target_chord['chord_name'],
                    'accuracy': comparison['accuracy'],
                    'feedback': comparison['feedback'],
                    'is_correct': comparison['is_correct']
                }
                logger.info(
                    f"Learning mode - Target chord: {target_chord['chord_name']}, "
                    f"Accuracy: {comparison['accuracy']:.1f}%")
                if len(comparison.get('feedback', [])) > 0:
                    logger.info("Feedback:")
                    for item in comparison['feedback']:
                        logger.info(f"  - {item}")
            else:
                # Normal mode: recognize the chord being played
                from utils.chord_recognition import recognize_chord

                chord_info = recognize_chord(finger_mappings, models['chord_recognizer'])
                logger.info(
                    f"Recognized chord: {chord_info['chord_name']} "
                    f"with confidence {chord_info.get('confidence', 0):.1f}%")

        except Exception as e:
            # logger.error(f"Error in chord recognition: {str(e)}")
            # We can still visualize finger positions
            try:
                vis_neck = hand_tracking.visualize_finger_mapping(
                    neck_crop, fretboard, finger_mappings)
                vis_frame[y1:y2, x1:x2] = vis_neck
            except:
                # Fallback to basic visualization
                try:
                    if 'string_fret_detector' in models and models['string_fret_detector'] is not None:
                        # YOLO 경로용 시각화 함수 사용
                        vis_neck = models['string_fret_detector'].visualize_fretboard(neck_crop, fretboard, angles=True)
                    else:
                        # 기존 방식 사용
                        vis_neck = fretboard_detection.visualize_fretboard(neck_crop, fretboard)
                    vis_frame[y1:y2, x1:x2] = vis_neck
                except:
                    pass

            if save_debug:
                vis_frame = render.create_status_bar(
                    vis_frame, f"Chord recognition error: {str(e)}", progress=80)
                result['visualization'] = vis_frame
            return result

        # Update result with chord recognition
        result['chord_recognized'] = chord_info['chord_name'] != 'Unknown'
        result['chord_info'] = chord_info
        result['success'] = True

        # Log success
        # logger.info("Frame processing completed successfully!")

        # Step 7: Create visualization
        # logger.info("Step 7: Creating visualization")
        try:
            # Visualize fretboard with finger positions
            # Check if the function accepts chord_info parameter
            import inspect
            vis_finger_params = inspect.signature(hand_tracking.visualize_finger_mapping).parameters

            if 'chord_info' in vis_finger_params:
                vis_neck = hand_tracking.visualize_finger_mapping(
                    neck_crop, fretboard, finger_mappings, chord_info)
            else:
                # Function doesn't accept chord_info
                vis_neck = hand_tracking.visualize_finger_mapping(
                    neck_crop, fretboard, finger_mappings)

            # Insert visualization into the frame
            vis_frame[y1:y2, x1:x2] = vis_neck

            # Add chord information and/or learning feedback
            if learning_mode and 'feedback' in chord_info:
                # Create feedback visualization for learning mode
                accuracy = chord_info.get('accuracy', 0)
                vis_frame = render.create_chord_feedback(
                    vis_frame,
                    target_chord['chord_name'],
                    chord_info['chord_name'],
                    accuracy
                )
            elif chord_info['chord_name'] != 'Unknown':
                # Add chord name for recognition mode
                confidence = chord_info.get('confidence', 0)
                display_text = f"Chord: {chord_info['chord_name']} ({confidence:.1f}%)"
                vis_frame = render.create_status_bar(vis_frame, display_text, progress=100)
            else:
                # Unknown chord
                vis_frame = render.create_status_bar(
                    vis_frame, "Playing unrecognized chord", progress=80)

        except Exception as e:
            # logger.error(f"Error in visualization: {str(e)}")
            # Use whatever visualization we have
            if save_debug:
                vis_frame = render.create_status_bar(
                    vis_frame, f"Visualization error: {str(e)}", progress=90)

        # Update result with visualization
        result['visualization'] = vis_frame
        return result

    except Exception as e:
        # logger.error(f"Unexpected error in frame processing: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())

        if save_debug:
            # Add error message to visualization
            vis_frame = render.create_status_bar(
                vis_frame, f"Error: {str(e)}", progress=0)
            result['visualization'] = vis_frame
        return result


def run_integrated_webcam_demo(models, camera_id=0, learning_mode=False, target_chord=None,
                               save_debug=False, use_audio=True):
    """
    간헐적 오디오 녹음 및 분석을 포함한 통합 웹캠 데모

    Args:
        models: 초기화된 모델 딕셔너리
        camera_id: 웹캠 ID
        learning_mode: 학습 모드 여부
        target_chord: 학습 모드에서의 목표 코드
        save_debug: 디버그 이미지 저장 여부
        use_audio: 오디오 인식 사용 여부
    """
    # 웹캠 열기
    logger.info(f"Opening webcam {camera_id}")
    cap = cv.VideoCapture(camera_id)
    if not cap.isOpened():
        logger.error(f"Could not open webcam {camera_id}")
        return

    # run_integrated_webcam_demo 함수 시작 부분에 추가
    logger.info(f"Audio enabled: {use_audio}")

    # 오디오 관련 변수 초기화
    audio_recorder = None
    current_audio = []
    audio_sr = 16000  # 샘플링 레이트

    # 상태 관련 변수
    STATE_IDLE = 0  # 대기 상태
    STATE_COUNTDOWN = 1  # 카운트다운 상태
    STATE_RECORDING = 2  # 녹음 상태
    STATE_ANALYZING = 3  # 분석 상태

    current_state = STATE_IDLE
    state_start_time = time.time()
    countdown_duration = 3  # 카운트다운 시간 (초)
    recording_duration = 5  # 녹음 시간 (초)
    result_display_duration = 3  # 결과 표시 시간 (초)

    analysis_result = None

    # 오디오 레코더 초기화
    # 오디오 파이프라인과 코드 인식기 확인
    if use_audio:
        # 오디오 파이프라인 확인 및 생성
        if 'audio_pipeline' not in models or models['audio_pipeline'] is None:
            logger.warning("Audio pipeline not found in models, creating a new one")
            try:
                from src.audio.audio_pipeline import GuitarAudioPipeline
                # 코드 인식기도 확인
                if 'chord_recognizer' not in models or models['chord_recognizer'] is None:
                    from src.audio.chord_recognition import GuitarChordRecognizer
                    logger.warning("Chord recognizer not found, creating a new one")
                    models['chord_recognizer'] = GuitarChordRecognizer()

                models['audio_pipeline'] = GuitarAudioPipeline()
                logger.info("Created new audio pipeline")
            except Exception as e:
                logger.error(f"Failed to create audio pipeline: {e}")
                use_audio = False

        try:
            from src.audio.audio_recorder import AudioRecorder
            logger.info("Initializing audio recorder")
            audio_recorder = AudioRecorder(callback=None)  # 콜백 사용 안 함
            if audio_recorder is not None:
                logger.info("Audio recorder initialized successfully")
            else:
                logger.error("Failed to initialize audio recorder - returned None")
                use_audio = False
        except Exception as e:
            logger.error(f"Error initializing audio recorder: {e}", exc_info=True)
            audio_recorder = None
            use_audio = False

    # 오디오 레코더 초기화 후
    logger.info(f"Audio recorder initialized: {audio_recorder is not None}")

    # 오디오 콜백 함수 (이제 직접 호출할 예정)
    def process_audio(audio_data, sr):
        try:
            logger.info(f"Processing audio data: length={len(audio_data)}, sr={sr}")
            # 오디오 데이터에 너무 많은 0이 있는지 확인
            silence_ratio = np.sum(np.abs(audio_data) < 0.01) / len(audio_data)
            logger.info(f"Silence ratio in audio: {silence_ratio:.2f}")

            # 임시 파이프라인 생성
            if 'audio_pipeline' not in models or models['audio_pipeline'] is None:
                from src.audio.audio_pipeline import GuitarAudioPipeline
                logger.info("Creating temporary audio pipeline")
                temp_pipeline = GuitarAudioPipeline()
                result = temp_pipeline.analyze_audio(audio_data, sr)
            else:
                result = models['audio_pipeline'].analyze_audio(audio_data, sr)

            logger.info(f"Raw analysis result: {result}")
            return result
        except Exception as e:
            logger.error(f"Error in process_audio: {e}", exc_info=True)  # 전체 스택트레이스 출력
            return {"detected_chord": f"Error: {str(e)}", "chord_similarity": 0.0}

    # FPS 계산 변수
    start_time = time.time()
    frame_count = 0
    fps = 0

    logger.info("Starting integrated webcam demo. Press 'q' to quit, 'l' for learning mode, 'r' to start recording.")

    while True:
        # 프레임 읽기
        ret, frame = cap.read()
        if not ret:
            logger.error("Could not read frame from webcam")
            break

        # 현재 시간 업데이트
        current_time = time.time()
        elapsed_since_state_start = current_time - state_start_time

        # 상태 전환 처리
        if current_state == STATE_IDLE:
            # 아무것도 하지 않음 (사용자 입력 대기)
            pass

        elif current_state == STATE_COUNTDOWN:
            # 카운트다운 표시
            remaining = countdown_duration - elapsed_since_state_start
            if remaining <= 0:
                # 카운트다운 종료, 녹음 시작
                current_state = STATE_RECORDING
                state_start_time = current_time

                # 오디오 녹음 시작
                if use_audio and audio_recorder:
                    current_audio = []  # 오디오 데이터 초기화
                    audio_recorder.start_recording()
                    logger.info("Recording started")

        elif current_state == STATE_RECORDING:
            # 녹음 진행 중 표시
            remaining = recording_duration - elapsed_since_state_start
            if remaining <= 0:
                # 녹음 종료, 분석 시작
                current_state = STATE_ANALYZING
                state_start_time = current_time

                # 오디오 녹음 종료 및 분석
                if use_audio and audio_recorder:
                    frames = audio_recorder.stop_recording()
                    logger.info("Recording stopped")

                    # 상태 변경 시
                    logger.info(f"Transitioning from RECORDING to ANALYZING")
                    logger.info(f"use_audio={use_audio}, audio_recorder exists={audio_recorder is not None}")

                    # 오디오 데이터 변환 및 분석
                    if frames:
                        try:
                            # 오디오 데이터를 numpy 배열로 변환
                            audio_data = np.frombuffer(b''.join(frames), dtype=np.float32)
                            logger.info(
                                f"Converted audio data: shape={audio_data.shape}, min={np.min(audio_data)}, max={np.max(audio_data)}")

                            # 오디오 데이터가 너무 작은지 확인
                            if np.max(np.abs(audio_data)) < 0.01:
                                logger.warning("Audio signal is too low. Check microphone or increase volume.")
                                analysis_result = {"detected_chord": "Signal too low", "chord_similarity": 0.0}
                            else:
                                # 오디오 분석
                                analysis_result = process_audio(audio_data, audio_sr)
                                logger.info(f"Audio analysis result: {analysis_result}")
                        except Exception as e:
                            logger.error(f"Error analyzing audio: {e}")
                            analysis_result = {"detected_chord": f"Error: {str(e)}", "chord_similarity": 0.0}
                    else:
                        logger.error("No audio data recorded")
                        analysis_result = {"detected_chord": "No audio", "chord_similarity": 0.0}
                else:
                    analysis_result = {"detected_chord": "Audio disabled", "chord_similarity": 0.0}

        elif current_state == STATE_ANALYZING:
            # 분석 결과 표시
            if elapsed_since_state_start >= result_display_duration:
                # 결과 표시 시간 종료, 대기 상태로 돌아감
                current_state = STATE_IDLE
                state_start_time = current_time

        # 비디오 프레임 처리 (기존 코드)
        result = process_frame(
            frame,
            models,
            learning_mode=learning_mode,
            target_chord=target_chord,
            save_debug=save_debug
        )

        # 화면 표시용 프레임 준비
        display_frame = result['visualization']

        # 상태에 따른 UI 추가
        if current_state == STATE_COUNTDOWN:
            # 카운트다운 표시
            remaining = int(countdown_duration - elapsed_since_state_start) + 1
            cv.putText(
                display_frame,
                f"Recording in {remaining}...",
                (display_frame.shape[1] // 2 - 150, display_frame.shape[0] // 2),
                cv.FONT_HERSHEY_SIMPLEX,
                1.5,
                (0, 0, 255),
                3
            )

        elif current_state == STATE_RECORDING:
            # 녹음 중 표시
            cv.putText(
                display_frame,
                "Recording...",
                (display_frame.shape[1] // 2 - 100, display_frame.shape[0] // 2),
                cv.FONT_HERSHEY_SIMPLEX,
                1.5,
                (0, 0, 255),
                3
            )

            # 진행 바 표시
            progress = min(1.0, elapsed_since_state_start / recording_duration)
            bar_width = int(display_frame.shape[1] * 0.6)
            bar_height = 20
            bar_x = (display_frame.shape[1] - bar_width) // 2
            bar_y = display_frame.shape[0] // 2 + 40

            # 배경 바
            cv.rectangle(
                display_frame,
                (bar_x, bar_y),
                (bar_x + bar_width, bar_y + bar_height),
                (100, 100, 100),
                -1
            )

            # 진행 바
            cv.rectangle(
                display_frame,
                (bar_x, bar_y),
                (bar_x + int(bar_width * progress), bar_y + bar_height),
                (0, 255, 0),
                -1
            )

        elif current_state == STATE_ANALYZING:
            # 분석 결과 표시
            if analysis_result:
                chord_name = analysis_result.get("detected_chord", "Unknown")
                similarity = analysis_result.get("chord_similarity", 0.0) * 100

                # 배경 패널
                panel_width = 400
                panel_height = 180
                panel_x = (display_frame.shape[1] - panel_width) // 2
                panel_y = (display_frame.shape[0] - panel_height) // 2

                cv.rectangle(
                    display_frame,
                    (panel_x, panel_y),
                    (panel_x + panel_width, panel_y + panel_height),
                    (50, 50, 50),
                    -1
                )

                # 결과 텍스트
                if chord_name in ["Unknown", "Error", "No audio", "Audio disabled"]:
                    # 인식 실패 메시지
                    cv.putText(
                        display_frame,
                        "Chord Recognition Failed",
                        (panel_x + 30, panel_y + 50),
                        cv.FONT_HERSHEY_SIMPLEX,
                        0.8,
                        (0, 0, 255),
                        2
                    )

                    cv.putText(
                        display_frame,
                        f"Reason: {chord_name}",
                        (panel_x + 30, panel_y + 100),
                        cv.FONT_HERSHEY_SIMPLEX,
                        0.7,
                        (200, 200, 200),
                        2
                    )
                else:
                    # 인식 성공 메시지
                    cv.putText(
                        display_frame,
                        "Detected Chord:",
                        (panel_x + 30, panel_y + 50),
                        cv.FONT_HERSHEY_SIMPLEX,
                        0.8,
                        (200, 200, 200),
                        2
                    )

                    cv.putText(
                        display_frame,
                        chord_name,
                        (panel_x + 30, panel_y + 110),
                        cv.FONT_HERSHEY_SIMPLEX,
                        1.5,
                        (0, 255, 0),
                        3
                    )

                    cv.putText(
                        display_frame,
                        f"Confidence: {similarity:.1f}%",
                        (panel_x + 30, panel_y + 150),
                        cv.FONT_HERSHEY_SIMPLEX,
                        0.7,
                        (200, 200, 200),
                        2
                    )

        # 항상 표시할 안내 메시지
        guide_y = display_frame.shape[0] - 30
        if current_state == STATE_IDLE:
            cv.putText(
                display_frame,
                "Press 'r' to start recording",
                (20, guide_y),
                cv.FONT_HERSHEY_SIMPLEX,
                0.7,
                (255, 255, 255),
                2
            )

        # FPS 계산
        frame_count += 1
        elapsed_time = time.time() - start_time
        if elapsed_time >= 1.0:
            fps = frame_count / elapsed_time
            frame_count = 0
            start_time = time.time()

        # FPS 표시
        cv.putText(
            display_frame,
            f"FPS: {fps:.1f}",
            (display_frame.shape[1] - 120, 30),
            cv.FONT_HERSHEY_SIMPLEX,
            0.7,
            (0, 255, 0),
            2
        )

        # 화면 표시
        cv.imshow("Guitar Chord Assistant", display_frame)

        # 키 입력 처리
        key = cv.waitKey(1) & 0xFF
        if key == ord('q'):
            break
        elif key == ord('l'):
            # 학습 모드 토글
            learning_mode = not learning_mode
            if learning_mode:
                from src.audio.chord_recognition import get_chord_for_learning

                target_chord = get_chord_for_learning('beginner')
                logger.info(f"Learning mode enabled. Target chord: {target_chord['chord_name']}")
            else:
                target_chord = None
                logger.info("Learning mode disabled")
        elif key == ord('r') and current_state == STATE_IDLE:
            # 녹음 시작 (카운트다운부터)
            current_state = STATE_COUNTDOWN
            state_start_time = time.time()
            logger.info("Starting countdown for recording")

    # 자원 해제
    cap.release()

    # 오디오 레코더 정지 (있는 경우)
    if audio_recorder is not None:
        audio_recorder.stop_recording()

    cv.destroyAllWindows()
    logger.info("Webcam demo stopped")


def run_api_server(models, host="0.0.0.0", port=8000):
    """
    Run the FastAPI server for the Guitar Chord Assistant.

    Args:
        models: Dictionary of initialized models
        host: Host to bind the server to
        port: Port to bind the server to
    """
    from fastapi_integration import app
    import uvicorn

    # Store models in app state
    app.state.models = models

    # Start the server
    logger.info(f"Starting API server on {host}:{port}")
    uvicorn.run("fastapi_integration:app", host=host, port=port, reload=False)


def run_websocket_server(models, host="0.0.0.0", port=8001):
    """
    Run the WebSocket server for real-time guitar chord recognition.

    Args:
        models: Dictionary of initialized models
        host: Host to bind the server to
        port: Port to bind the server to
    """
    # Import here to avoid loading websocket modules when not needed
    from fastapi_websocket import create_websocket_app
    import uvicorn

    # Create WebSocket app
    app = create_websocket_app()

    # Store models in app state
    app.state.models = models

    # Start the server
    logger.info(f"Starting WebSocket server on {host}:{port}")
    uvicorn.run(app, host=host, port=port)


def train_models(dataset_dir, output_dir, model_type="neck", epochs=100, batch_size=16):
    """
    Train the guitar detection models.

    Args:
        dataset_dir: Path to the dataset directory
        output_dir: Path to save trained models
        model_type: Type of model to train ("neck" or "string_fret")
        epochs: Number of training epochs
        batch_size: Batch size for training
    """
    # Set up output directory
    os.makedirs(output_dir, exist_ok=True)

    if model_type == "neck":
        # Train guitar neck detection model
        from src.detection.guitar_detection_training import main as train_neck

        logger.info(f"Training guitar neck detection model with {dataset_dir} dataset")
        model_path = train_neck(dataset_dir, output_dir,
                                yaml_path=f'guitar_neck_data.yaml',  # 고유한 YAML 파일 이름
                                epochs=epochs, batch_size=batch_size, logger=logger)

        logger.info(f"Guitar neck detection model training completed. Model saved at: {model_path}")
        return model_path

    elif model_type == "string_fret":
        # Train string/fret detection model
        from src.detection.string_fret_detection_training import main as train_string_fret

        logger.info(f"Training string and fret detection model with {dataset_dir} dataset")
        # 로거 객체를 새로운 키워드 인수로 전달
        # 하지만 직접 전달하지 말고 로깅 설정만 조정

        # 먼저 현재 로깅 핸들러 저장
        existing_handlers = logger.handlers.copy()

        model_path = train_string_fret(dataset_dir, output_dir,
                                       yaml_path=f'string_fret_data.yaml',
                                       epochs=epochs, batch_size=batch_size)

        logger.info(f"String and fret detection model training completed. Model saved at: {model_path}")
        return model_path

    else:
        logger.error(f"Unknown model type: {model_type}")
        raise ValueError(f"Unknown model type: {model_type}")


def train_audio_model():
    """
    CREPE 패키지 설치 확인 및 테스트
    """
    logger.info("CREPE 라이브러리 확인 중...")

    try:
        import crepe

        # 모델 테스트
        logger.info("CREPE 모델 테스트 중...")
        dummy_audio = np.zeros(16000, dtype=np.float32)  # 1초 길이 더미 오디오
        crepe.predict(dummy_audio, 16000, model_capacity="tiny", step_size=1000, verbose=False)

        logger.info("CREPE 모델 테스트 완료 - 정상적으로 작동합니다.")
        return True

    except ImportError:
        logger.error("CREPE 패키지가 설치되어 있지 않습니다.")
        logger.error("다음 명령어로 설치하세요: pip install crepe")
        return False

    except Exception as e:
        logger.error(f"CREPE 모델 테스트 중 오류 발생: {e}")
        return False


def run_audio_demo():
    """
    오디오 기반 코드 인식 데모 실행
    """
    from src.audio.audio_pipeline import GuitarAudioPipeline
    from src.audio.audio_recorder import AudioRecorder
    import matplotlib.pyplot as plt
    import time

    logger.info("오디오 기반 기타 코드 인식 데모 시작")

    # 오디오 파이프라인 초기화
    pipeline = GuitarAudioPipeline()

    # # CREPE 모델이 없으면 다운로드
    # if not os.path.exists(pipeline.pitch_detector.model_path):
    #     logger.info("CREPE 모델 다운로드 중...")
    #     pipeline.download_crepe_model(model_size="tiny")

    # 결과 저장을 위한 클래스 정의
    class ResultTracker:
        def __init__(self):
            self.results = []
            self.timestamps = []
            self.similarities = []
            self.detected_chords = []
            self.current_chord = "Unknown"

    # 결과 트래커 인스턴스 생성
    tracker = ResultTracker()

    # 그래프 초기화
    plt.ion()  # 인터랙티브 모드 활성화
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 8))
    fig.suptitle("실시간 기타 코드 인식", fontsize=16)

    # 콜백 함수
    # 콜백 함수 - tracker를 인자로 전달받음
    def audio_callback(audio_data, sr):
        try:
            result = pipeline.analyze_audio(audio_data, sr)

            # 결과가 충분히 신뢰할 수 있는 경우만 저장
            if result["chord_similarity"] > 0.5:
                tracker.results.append(result)
                tracker.timestamps.append(time.time())
                tracker.similarities.append(result["chord_similarity"])
                tracker.detected_chords.append(result["detected_chord"])

                # 최근 3초 데이터만 유지
                current_time = time.time()
                cutoff_time = current_time - 3

                keep_indices = [i for i, t in enumerate(tracker.timestamps) if t >= cutoff_time]
                if keep_indices:
                    tracker.timestamps = [tracker.timestamps[i] for i in keep_indices]
                    tracker.similarities = [tracker.similarities[i] for i in keep_indices]
                    tracker.detected_chords = [tracker.detected_chords[i] for i in keep_indices]
                    tracker.results = [tracker.results[i] for i in keep_indices]

                # 가장 최근 감지 코드
                if tracker.detected_chords:
                    from collections import Counter
                    counts = Counter(
                        tracker.detected_chords[-5:] if len(tracker.detected_chords) >= 5 else tracker.detected_chords)
                    tracker.current_chord = counts.most_common(1)[0][0]

                    logger.info(f"감지된 코드: {tracker.current_chord} (유사도: {result['chord_similarity']:.2f})")

                    # 그래프 업데이트
                    update_plot()
        except Exception as e:
            logger.error(f"오디오 분석 오류: {e}")
            import traceback
            logger.error(traceback.format_exc())

    # 그래프 업데이트 함수 - 클래스 인스턴스 사용
    def update_plot():
        if tracker.timestamps:
            # 상대 시간으로 변환
            t0 = tracker.timestamps[0]
            relative_times = [t - t0 for t in tracker.timestamps]

            # 유사도 그래프 업데이트
            ax1.clear()
            ax1.plot(relative_times, tracker.similarities, 'b-')
            ax1.set_xlim(max(0, relative_times[-1] - 10) if relative_times else 0,
                         relative_times[-1] + 0.5 if relative_times else 10)
            ax1.set_ylim(0, 1)
            ax1.set_xlabel("시간 (초)")
            ax1.set_ylabel("유사도")
            ax1.set_title("코드 인식 유사도")
            ax1.grid(True)

            # 코드 텍스트 업데이트
            ax2.clear()
            ax2.text(0.5, 0.5, tracker.current_chord, fontsize=40, ha='center', va='center')
            ax2.set_xlim(0, 1)
            ax2.set_ylim(0, 1)
            ax2.axis('off')

            # 최근 감지된 코드 표시
            if len(tracker.detected_chords) >= 3:
                recent = tracker.detected_chords[-3:]
                recent_text = f"최근 감지: {', '.join(recent)}"
                ax2.text(0.5, 0.2, recent_text, fontsize=12, ha='center', va='center')

            # 그래프 갱신
            fig.canvas.draw_idle()
            fig.canvas.flush_events()

    # 그래프 초기화
    plt.ion()  # 인터랙티브 모드 활성화
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 8))
    fig.suptitle("실시간 기타 코드 인식", fontsize=16)

    # 오디오 레코더 초기화 - 콜백 함수 전달
    recorder = AudioRecorder(callback=audio_callback)

    try:
        # 녹음 시작
        recorder.start_recording()
        logger.info("녹음 시작됨. 종료하려면 Ctrl+C를 누르세요.")

        # 메인 루프
        while True:
            time.sleep(0.1)  # CPU 사용률 감소

            # 종료 확인 (matplotlib 창이 닫혔는지)
            if not plt.fignum_exists(fig.number):
                break

    except KeyboardInterrupt:
        logger.info("사용자에 의해 종료됨")

    finally:
        # 녹음 중지
        recorder.stop_recording()

        # 결과 저장 (충분한 결과가 있는 경우)
        if tracker.results and len(tracker.results) > 10:
            logger.info("녹음 세션 저장 중...")
            recorder.save_wav("recorded_session.wav")

            # 가장 많이 감지된 코드 찾기
            from collections import Counter
            chord_counts = Counter([r["detected_chord"] for r in tracker.results])
            most_common = chord_counts.most_common(3)

            logger.info(f"\n분석 결과:")
            logger.info(f"가장 많이 감지된 코드: {most_common}")
            logger.info(f"총 분석 프레임: {len(tracker.results)}")

        plt.close(fig)
        logger.info("오디오 데모 종료")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Guitar Chord Assistant")

    # Main command
    subparsers = parser.add_subparsers(dest="command", help="Command to run")

    # Train command
    train_parser = subparsers.add_parser("train", help="Train the guitar detection model")
    train_parser.add_argument("--dataset", type=str, required=True, help="Path to dataset directory")
    train_parser.add_argument("--output", type=str, default="models", help="Path to save trained models")
    train_parser.add_argument("--epochs", type=int, default=100, help="Number of training epochs")
    train_parser.add_argument("--batch-size", type=int, default=16, help="Batch size for training")
    train_parser.add_argument("--model-type", type=str, choices=["neck", "string_fret"], default="neck",
                              help="Type of model to train (neck or string_fret)")

    # Train audio model command
    train_audio_parser = subparsers.add_parser("train-audio", help="Download and convert audio model")

    # Webcam demo command
    webcam_parser = subparsers.add_parser("webcam", help="Run the webcam demo")
    webcam_parser.add_argument("--model", type=str, default="models/guitar_neck_detection/train/weights/best.pt",
                               help="Path to trained guitar detection model")
    webcam_parser.add_argument("--string-fret-model", type=str,
                               default="models/string_fret_detection/train/weights/best.pt",
                               help="Path to trained string-fret detection model")
    webcam_parser.add_argument("--camera", type=int, default=0, help="Camera ID for webcam")
    webcam_parser.add_argument("--learning", action="store_true", help="Start in learning mode")
    webcam_parser.add_argument("--debug", action="store_true", help="Save debug images")
    webcam_parser.add_argument("--cpu", action="store_true", help="Force CPU mode (no GPU)")
    webcam_parser.add_argument("--no-audio", action="store_true", help="Disable audio recognition")

    # Audio demo command
    audio_parser = subparsers.add_parser("audio", help="Run the audio demo")

    # API server command
    api_parser = subparsers.add_parser("api", help="Run the FastAPI server")
    api_parser.add_argument("--host", type=str, default="0.0.0.0", help="Host to run the API server on")
    api_parser.add_argument("--port", type=int, default=8000, help="Port to run the API server on")
    api_parser.add_argument("--model", type=str, default="models/guitar_neck_detection/train/weights/best.pt",
                            help="Path to trained guitar detection model")
    api_parser.add_argument("--cpu", action="store_true", help="Force CPU mode (no GPU)")

    # WebSocket server command
    websocket_parser = subparsers.add_parser("websocket", help="Run the WebSocket server")
    websocket_parser.add_argument("--host", type=str, default="0.0.0.0", help="Host to run the WebSocket server on")
    websocket_parser.add_argument("--port", type=int, default=8001, help="Port to run the WebSocket server on")
    websocket_parser.add_argument("--model", type=str, default="models/guitar_neck_detection/train/weights/best.pt",
                                  help="Path to trained guitar detection model")
    websocket_parser.add_argument("--cpu", action="store_true", help="Force CPU mode (no GPU)")

    # Parse arguments
    args = parser.parse_args()

    # Execute the specified command
    if args.command == "train":
        train_models(args.dataset, args.output, model_type=args.model_type,
                     epochs=args.epochs, batch_size=args.batch_size)

    elif args.command == "train-audio":
        train_audio_model()

    elif args.command == "audio":
        run_audio_demo()

    elif args.command == "webcam":
        # Check if model exists
        if not os.path.exists(args.model):
            logger.error(f"Model file not found: {args.model}")
            logger.info("Please train the model first with the 'train' command")
            sys.exit(1)

        # 모델 초기화
        models = setup_models(
            args.model,  # 넥 감지 모델
            args.string_fret_model,  # 스트링/프렛 감지 모델 (추가됨)
            use_gpu=not args.cpu,
            use_audio=not args.no_audio
        )

        # Initialize target chord if learning mode
        target_chord = None
        if args.learning:
            from utils.chord_recognition import get_chord_for_learning

            target_chord = get_chord_for_learning('beginner')
            logger.info(f"Learning mode enabled. Target chord: {target_chord['chord_name']}")

        # Run the integrated webcam demo
        run_integrated_webcam_demo(
            models,
            camera_id=args.camera,
            learning_mode=args.learning,
            target_chord=target_chord,
            save_debug=args.debug,
            use_audio=not args.no_audio
        )

    elif args.command == "api":
        # Check if model exists
        if not os.path.exists(args.model):
            logger.error(f"Model file not found: {args.model}")
            logger.info("Please train the model first with the 'train' command")
            sys.exit(1)

        # Initialize models
        models = setup_models(args.model, use_gpu=not args.cpu)

        # Run the API server
        run_api_server(models, host=args.host, port=args.port)

    elif args.command == "websocket":
        # Check if model exists
        if not os.path.exists(args.model):
            logger.error(f"Model file not found: {args.model}")
            logger.info("Please train the model first with the 'train' command")
            sys.exit(1)

        # Initialize models
        models = setup_models(args.model, use_gpu=not args.cpu)

        # Run the WebSocket server
        run_websocket_server(models, host=args.host, port=args.port)

    else:
        parser.print_help()
"""
Guitar strings and frets detection module using YOLOv12.

This module provides functions to detect guitar strings and frets using
a trained YOLOv12 model, replacing the Hough transform approach.
"""

import os
import cv2 as cv
import numpy as np
import torch
from ultralytics import YOLO
import logging

# Configure logging
logger = logging.getLogger("string-fret-detector")

# Default temporary directory for debug images
TMP_DIR = os.path.join("temp", "")


def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


class StringFretDetector:
    """
    Guitar strings and frets detector using YOLOv12.

    This class loads a trained YOLOv12 model and provides methods
    to detect guitar strings and frets in neck images.
    """

    def __init__(self, model_path="models/string_fret_detection/train/weights/best.pt",
                 conf_threshold=0.25, device=None):
        """
        Initialize the strings and frets detector.

        Args:
            model_path: Path to the trained YOLOv12 model
            conf_threshold: Confidence threshold for detections
            device: Device to run inference on ('cpu', '0', etc.)
        """
        # Select device
        if device is None:
            self.device = '0' if torch.cuda.is_available() else 'cpu'
        else:
            self.device = device

        # logger.info(f"Loading YOLO model from {model_path} on device {self.device}")

        # Load model
        try:
            self.model = YOLO(model_path)
            logger.info("String-Fret detector model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading model: {str(e)}")
            raise e

        self.conf_threshold = conf_threshold
        self.class_names = ['fret', 'nut', 'string']  # Based on training data

    def detect(self, neck_image, save_debug=False):
        """
        Detect strings and frets in a neck image.

        Args:
            neck_image: Image of guitar neck (numpy array)
            save_debug: Whether to save debug images

        Returns:
            Dictionary with detected strings and frets
        """
        # Run detection on the neck image
        results = self.model(neck_image, conf=self.conf_threshold, device=self.device)

        # Process results
        strings = []
        frets = []
        nuts = []

        # 원시 좌표 저장을 위한 리스트
        string_points = []
        fret_points = []
        nut_points = []

        print(f"YOLO DETECTOR DEBUG: Model returned {len(results)} results")

        for r in results:
            boxes = r.boxes
            print(f"YOLO DETECTOR DEBUG: Found {len(boxes)} boxes")

            if len(boxes) == 0:
                continue

            # Get the original image for visualization
            orig_img = r.orig_img
            height, width = orig_img.shape[:2]
            print(f"YOLO DETECTOR DEBUG: Image shape: {height}x{width}")

            # Process each detection
            for i, box in enumerate(boxes):
                # Get class and confidence
                cls = int(box.cls[0].item())
                conf = float(box.conf[0].item())
                class_name = self.class_names[cls] if cls < len(self.class_names) else f"unknown-{cls}"

                # Get bounding box
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy().astype(int)

                print(
                    f"YOLO DETECTOR DEBUG: Box {i}: class={class_name}, conf={conf:.2f}, coords=({x1},{y1},{x2},{y2})")

                # 박스 중심점 계산
                center_x = (x1 + x2) / 2
                center_y = (y1 + y2) / 2

                # 박스 크기 계산
                box_width = x2 - x1
                box_height = y2 - y1

                print(
                    f"YOLO DETECTOR DEBUG: Box {i}: center=({center_x:.1f},{center_y:.1f}), size={box_width}x{box_height}")

                # Categorize based on class
                if class_name == 'string':
                    if box_width > box_height:  # 문자열은 일반적으로 가로로 긴 박스
                        # Hough 변환 형식 호환을 위한 라인 파라미터
                        rho = center_y
                        theta = np.pi / 2
                        strings.append(np.array([[rho, theta]]))

                        # 원시 좌표점 저장 (왼쪽과 오른쪽 끝점)
                        points = [(x1, center_y), (x2, center_y)]
                        string_points.append(points)
                        print(f"YOLO DETECTOR DEBUG: Added string: points={points}")
                    else:
                        print(f"YOLO DETECTOR DEBUG: Unusual vertical string detected, skipping")

                elif class_name == 'fret':
                    if box_width < box_height:  # 프렛은 일반적으로 세로로 긴 박스
                        # Hough 변환 형식 호환을 위한 라인 파라미터
                        rho = center_x
                        theta = 0.0  # 수직선
                        frets.append(np.array([[rho, theta]]))

                        # 원시 좌표점 저장 (위와 아래 끝점)
                        points = [(center_x, y1), (center_x, y2)]
                        fret_points.append(points)
                        print(f"YOLO DETECTOR DEBUG: Added fret: points={points}")
                    else:
                        print(f"YOLO DETECTOR DEBUG: Unusual horizontal fret detected, skipping")

                elif class_name == 'nut':
                    nuts.append((x1, y1, x2, y2))

                    # 원시 좌표점 저장
                    points = [(x1, y1), (x2, y2)]
                    nut_points.append(points)
                    print(f"YOLO DETECTOR DEBUG: Added nut: points={points}")

        # Convert lists to numpy arrays for consistency with original code
        strings_array = np.array(strings) if strings else None
        frets_array = np.array(frets) if frets else None

        # 최종 결과 로깅
        print(f"YOLO DETECTOR DEBUG: Final counts - strings: {len(strings)}, frets: {len(frets)}, nuts: {len(nuts)}")
        print(f"YOLO DETECTOR DEBUG: String points: {len(string_points)}")
        print(f"YOLO DETECTOR DEBUG: Fret points: {len(fret_points)}")

        # Create debug visualization if requested
        if save_debug:
            try:
                self._save_debug_visualization(neck_image, strings_array, frets_array, nuts,
                                               string_points, fret_points)
                print("YOLO DETECTOR DEBUG: Debug visualization saved")
            except Exception as e:
                print(f"YOLO DETECTOR DEBUG: Error saving visualization: {e}")

        # Log detection statistics
        logger.info(f"Detected {len(strings)} strings, {len(frets)} frets, and {len(nuts)} nuts")

        # 반환 값 생성 전 구조 확인
        raw_detections = {
            'string_points': string_points,
            'fret_points': fret_points,
            'nut_points': nut_points
        }
        print(f"YOLO DETECTOR DEBUG: raw_detections structure created")

        return {
            'strings': strings_array,
            'frets': frets_array,
            'nuts': nuts,
            'raw_detections': raw_detections
        }

    def _save_debug_visualization(self, neck_image, strings, frets, nuts,
                                  string_points, fret_points):
        """
        저장 디버그 시각화 이미지
        """
        import cv2 as cv
        import os

        # 임시 디렉토리 생성
        temp_dir = "temp"
        os.makedirs(temp_dir, exist_ok=True)

        debug_img = neck_image.copy()
        height, width = debug_img.shape[:2]

        # 문자열 그리기 (녹색)
        if string_points:
            for points in string_points:
                # 원본 감지 포인트 그리기
                for x, y in points:
                    cv.circle(debug_img, (int(x), int(y)), 3, (0, 255, 255), -1)

                # 포인트를 연결하는 선 그리기
                x1, y1 = points[0]
                x2, y2 = points[-1]
                cv.line(debug_img, (int(x1), int(y1)), (int(x2), int(y2)), (0, 255, 0), 2)

        # 프렛 그리기 (빨강)
        if fret_points:
            for points in fret_points:
                # 원본 감지 포인트 그리기
                for x, y in points:
                    cv.circle(debug_img, (int(x), int(y)), 3, (255, 255, 0), -1)

                # 포인트를 연결하는 선 그리기
                x1, y1 = points[0]
                x2, y2 = points[-1]
                cv.line(debug_img, (int(x1), int(y1)), (int(x2), int(y2)), (0, 0, 255), 2)

        # 너트 그리기 (파랑)
        for x1, y1, x2, y2 in nuts:
            cv.rectangle(debug_img, (x1, y1), (x2, y2), (255, 0, 0), 2)

        # 디버그 이미지 저장
        cv.imwrite(os.path.join(temp_dir, 'yolo_string_fret_detection.jpg'), debug_img)


    def visualize_fretboard(self, image, fretboard, angles=True):
        """
        Visualize the fretboard with detected strings and frets,
        supporting angled lines if requested.

        Args:
            image: Original image
            fretboard: Fretboard data structure
            angles: If True, use actual line angles

        Returns:
            Visualization image
        """
        import math
        import logging
        logger = logging.getLogger("string-fret-detector")
        logger.info("StringFretDetector visualize_fretboard called")

        if image is None:
            return None

        visualization = image.copy()
        height, width = visualization.shape[:2]

        # 기울어진 선 그리기 (각도 정보 사용)
        if angles and 'string_lines' in fretboard and fretboard['string_lines'] is not None:
            logger.info(f"Drawing {len(fretboard['string_lines'])} strings with angles")
            max_length = math.sqrt(height ** 2 + width ** 2)

            for line in fretboard['string_lines']:
                rho, theta = line[0]
                # 선의 끝점 계산
                a = np.cos(theta)
                b = np.sin(theta)
                x0 = a * rho
                y0 = b * rho

                # 이미지 경계까지 연장된 선분의 끝점
                x1 = int(x0 + max_length * (-b))
                y1 = int(y0 + max_length * (a))
                x2 = int(x0 - max_length * (-b))
                y2 = int(y0 - max_length * (a))

                # 이미지 경계로 클리핑
                p1 = (max(0, min(width - 1, x1)), max(0, min(height - 1, y1)))
                p2 = (max(0, min(width - 1, x2)), max(0, min(height - 1, y2)))

                # 선 그리기
                cv.line(visualization, p1, p2, (0, 255, 0), 1)

        # 수직 선 (frets)
        if angles and 'fret_lines' in fretboard and fretboard['fret_lines'] is not None:
            logger.info(f"Drawing {len(fretboard['fret_lines'])} frets with angles")
            max_length = math.sqrt(height ** 2 + width ** 2)

            for line in fretboard['fret_lines']:
                rho, theta = line[0]
                # 선의 끝점 계산
                a = np.cos(theta)
                b = np.sin(theta)
                x0 = a * rho
                y0 = b * rho

                # 이미지 경계까지 연장된 선분의 끝점
                x1 = int(x0 + max_length * (-b))
                y1 = int(y0 + max_length * (a))
                x2 = int(x0 - max_length * (-b))
                y2 = int(y0 - max_length * (a))

                # 이미지 경계로 클리핑
                p1 = (max(0, min(width - 1, x1)), max(0, min(height - 1, y1)))
                p2 = (max(0, min(width - 1, x2)), max(0, min(height - 1, y2)))

                # 선 그리기
                cv.line(visualization, p1, p2, (0, 0, 255), 1)

        # 기존 방식으로 백업 그리기 (항상 보여주기)
        if 'string_positions' in fretboard and fretboard['string_positions']:
            logger.info(f"Drawing {len(fretboard['string_positions'])} strings with positions")
            for y in fretboard['string_positions']:
                cv.line(visualization, (0, y), (width, y), (0, 255, 0), 1)

        if 'fret_positions' in fretboard and fretboard['fret_positions']:
            logger.info(f"Drawing {len(fretboard['fret_positions'])} frets with positions")
            for x in fretboard['fret_positions']:
                cv.line(visualization, (x, 0), (x, height), (0, 0, 255), 1)

        return visualization


# Function to replace the original detect_strings_and_frets in fretboard_detection.py
def detect_strings_and_frets_yolo(neck_image, model=None, conf_threshold=0.25,
                                  verbose=False, save_debug=False):
    """
    Detect guitar strings and frets in a neck image using YOLO.

    Args:
        neck_image: Image of guitar neck (numpy array or torch tensor)
        model: Pre-loaded StringFretDetector instance
        conf_threshold: Confidence threshold for detections
        verbose: If True, print detailed information
        save_debug: If True, save debug images

    Returns:
        Dictionary with 'strings' and 'frets' keys containing detected lines
    """
    # Convert to numpy if needed
    if torch.is_tensor(neck_image):
        neck_image = neck_image.numpy()

    # Create detector if not provided
    if model is None:
        model_path = "models/string_fret_detection/train/weights/best.pt"
        if not os.path.exists(model_path):
            logger.warning(f"String-fret detection model not found at {model_path}")
            logger.warning("Falling back to Hough transform method")
            # Import original function from fretboard_detection
            from src.detection.fretboard_detection import detect_strings_and_frets as detect_hough
            return detect_hough(neck_image, verbose=verbose, save_debug=save_debug)

        model = StringFretDetector(model_path, conf_threshold)

    # Detect strings and frets
    results = model.detect(neck_image, save_debug=save_debug)

    if verbose:
        string_count = 0 if results['strings'] is None else len(results['strings'])
        fret_count = 0 if results['frets'] is None else len(results['frets'])
        logger.info(f"YOLO detected {string_count} strings and {fret_count} frets")

    return results


# Function to integrate with the main pipeline
def get_string_fret_detector(model_path=None):
    """
    Get a string and fret detector instance.

    Args:
        model_path: Path to the trained model

    Returns:
        StringFretDetector instance or None if model not found
    """
    if model_path is None:
        model_path = "models/string_fret_detection/train/weights/best.pt"

    if os.path.exists(model_path):
        return StringFretDetector(model_path)
    else:
        logger.warning(f"String-fret detection model not found at {model_path}")
        return None


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Test string and fret detection on an image')
    parser.add_argument('--image', type=str, required=True, help='Path to neck image')
    parser.add_argument('--model', type=str, default='models/string_fret_detection/train/weights/best.pt',
                        help='Path to trained YOLO model')
    parser.add_argument('--conf', type=float, default=0.25, help='Confidence threshold')
    parser.add_argument('--save', action='store_true', help='Save debug visualization')

    args = parser.parse_args()

    # Configure logging
    logging.basicConfig(level=logging.INFO)

    # Load image
    img = cv.imread(args.image)
    if img is None:
        print(f"Could not read image: {args.image}")
        exit(1)

    # Create detector
    detector = StringFretDetector(args.model, args.conf)

    # Detect strings and frets
    results = detector.detect(img, save_debug=args.save)

    print(f"Detected {0 if results['strings'] is None else len(results['strings'])} strings")
    print(f"Detected {0 if results['frets'] is None else len(results['frets'])} frets")

    if args.save:
        print(f"Debug visualization saved to {os.path.join(TMP_DIR, 'yolo_string_fret_detection.jpg')}")
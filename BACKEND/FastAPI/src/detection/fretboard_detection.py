"""
Fretboard detection module for guitar chord recognition.

This module provides functions to detect guitar strings and frets,
and to create a virtual fretboard model for chord analysis.
"""

import os
import cv2 as cv
import numpy as np
import math
import torch

from src.processing.image_processing import frei_and_chen_edges
from src.processing.geometric_transform import detect_lines, filter_lines, line_points

# Default temporary directory for debug images
TMP_DIR = os.path.join("temp", "")


def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


def detect_strings_and_frets(neck_image, string_threshold=250, fret_threshold=200,
                             verbose=False, save_debug=False, yolo_detector=None):
    """
    Detect guitar strings and frets in a neck image.

    Args:
        neck_image: Image of guitar neck (numpy array or torch tensor)
        string_threshold: Threshold for string detection
        fret_threshold: Threshold for fret detection
        verbose: If True, print detailed information
        save_debug: If True, save debug images
        yolo_detector: Optional YOLO model for string/fret detection

    Returns:
        Dictionary with 'strings' and 'frets' keys containing detected lines
    """
    # Create temp directory if needed
    if save_debug:
        ensure_temp_dir()

    # Convert to numpy if needed
    if isinstance(neck_image, torch.Tensor):
        neck_image = neck_image.numpy()

    # 이미지 크기 가져오기
    height, width = neck_image.shape[:2]

    # Try YOLO detection first if model is provided
    if yolo_detector is not None:
        try:
            if verbose:
                print("Using YOLO model for string and fret detection")

            detection_result = yolo_detector.detect(neck_image, save_debug=save_debug)
            horizontal_lines = detection_result['strings']
            vertical_lines = detection_result['frets']

            if verbose:
                string_count = 0 if horizontal_lines is None else len(horizontal_lines)
                fret_count = 0 if vertical_lines is None else len(vertical_lines)
                print(f"YOLO detected {string_count} strings and {fret_count} frets")

            # If YOLO detection fails to find enough lines, fall back to Hough transform
            if (horizontal_lines is None or len(horizontal_lines) < 3 or
                    vertical_lines is None or len(vertical_lines) < 3):
                if verbose:
                    print("Insufficient YOLO detections, falling back to Hough transform")
            else:
                # YOLO detection was successful
                return {
                    'strings': horizontal_lines,
                    'frets': vertical_lines
                }
        except Exception as e:
            if verbose:
                print(f"Error in YOLO detection: {str(e)}, falling back to Hough transform")

    # Fallback to traditional Hough transform method
    if verbose:
        print("Using Hough transform for string and fret detection")

    # Get edges for line detection
    edge_image = frei_and_chen_edges(torch.from_numpy(neck_image)).numpy()

    # Detect lines with Hough transform
    lines = cv.HoughLines(edge_image, 1, np.pi / 180, min(string_threshold, fret_threshold))

    # Process detected lines
    horizontal_lines = []
    vertical_lines = []

    if lines is not None:
        for line in lines:
            rho, theta = line[0]
            # Convert to degrees for easier interpretation
            angle_deg = (theta * 180 / np.pi) - 90

            # Identify string candidates (roughly horizontal lines)
            if -45 <= angle_deg <= 45:  # 확장된 각도 범위
                horizontal_lines.append(line)
            # Identify fret candidates (roughly vertical lines)
            elif 45 <= abs(angle_deg) <= 90:
                vertical_lines.append(line)

    horizontal_lines = np.array(horizontal_lines) if horizontal_lines else None
    vertical_lines = np.array(vertical_lines) if vertical_lines else None

    # Filter out spurious detections based on consistent angles
    if horizontal_lines is not None and len(horizontal_lines) > 3:
        horizontal_lines = filter_lines_by_angle(horizontal_lines)

    if vertical_lines is not None and len(vertical_lines) > 3:
        vertical_lines = filter_lines_by_angle(vertical_lines)

    if verbose:
        string_count = 0 if horizontal_lines is None else len(horizontal_lines)
        fret_count = 0 if vertical_lines is None else len(vertical_lines)
        print(f"Hough transform detected {string_count} strings and {fret_count} frets")

    # Draw debug image
    if save_debug and (horizontal_lines is not None or vertical_lines is not None):
        debug_image = neck_image.copy()
        max_length = math.sqrt(neck_image.shape[0] ** 2 + neck_image.shape[1] ** 2)

        # Draw strings
        if horizontal_lines is not None:
            for line in horizontal_lines:
                p1, p2 = line_points(line, max_length)
                cv.line(debug_image, p1, p2, (0, 255, 0), 2)  # Green for strings

                # 디버그: 각도 표시
                rho, theta = line[0]
                angle_deg = (theta * 180 / np.pi) - 90
                mid_point = ((p1[0] + p2[0]) // 2, (p1[1] + p2[1]) // 2)
                cv.putText(debug_image, f"{angle_deg:.1f}°", mid_point,
                           cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

        # Draw frets
        if vertical_lines is not None:
            for line in vertical_lines:
                p1, p2 = line_points(line, max_length)
                cv.line(debug_image, p1, p2, (0, 0, 255), 2)  # Red for frets

                # 디버그: 각도 표시
                rho, theta = line[0]
                angle_deg = (theta * 180 / np.pi) - 90
                mid_point = ((p1[0] + p2[0]) // 2, (p1[1] + p2[1]) // 2)
                cv.putText(debug_image, f"{angle_deg:.1f}°", mid_point,
                           cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

        cv.imwrite(os.path.join(TMP_DIR, 'fretboard_detection.jpg'), debug_image)

    return {
        'strings': horizontal_lines,
        'frets': vertical_lines
    }


def filter_lines_by_angle(lines, angle_tolerance=10):
    """
    Filter lines to keep only those with similar angles.

    Args:
        lines: Array of lines from HoughLines
        angle_tolerance: Maximum angle difference in degrees

    Returns:
        Filtered array of lines
    """
    if lines is None or len(lines) <= 3:
        return lines

    # 각도들을 추출
    angles = []
    for line in lines:
        _, theta = line[0]
        angle_deg = (theta * 180 / np.pi) - 90
        angles.append(angle_deg)

    # 중앙값 각도 계산
    median_angle = np.median(angles)

    # 중앙값과 비슷한 각도의 선들만 유지
    filtered_lines = []
    for i, line in enumerate(lines):
        if abs(angles[i] - median_angle) <= angle_tolerance:
            filtered_lines.append(line)

    return np.array(filtered_lines)


def optimize_string_detection(strings, expected_count=6, height=None):
    """
    Optimize string detection results to find the most likely 6 strings.

    Args:
        strings: Detected horizontal lines
        expected_count: Expected number of strings (default 6)
        height: Height of the image (for spacing estimation)

    Returns:
        Optimized array of string lines
    """
    if strings is None or len(strings) == 0:
        return None

    # If we have exactly the expected number, return as is
    if len(strings) == expected_count:
        return strings

    # If we have fewer than expected, we'll need to interpolate
    if len(strings) < expected_count:
        return interpolate_missing_strings(strings, expected_count, height)

    # If we have more than expected, we need to select the most likely ones
    # Sort by rho value (y-position)
    strings_sorted = sorted(strings, key=lambda line: line[0][0])

    # For guitar, the 6 strings should be roughly equally spaced
    # Try different combinations of 6 strings and find the one with most regular spacing

    # Simple approach: take strings at regular intervals
    indices = np.linspace(0, len(strings_sorted) - 1, expected_count).astype(int)
    selected_strings = [strings_sorted[i] for i in indices]

    return np.array(selected_strings)


def interpolate_missing_strings(strings, expected_count, height):
    """
    Interpolate missing strings when fewer than expected are detected.

    Args:
        strings: Detected string lines
        expected_count: Expected number of strings
        height: Height of the image

    Returns:
        Array with interpolated strings
    """
    if strings is None or len(strings) == 0:
        # No strings detected, create evenly spaced strings
        if height is None:
            return None

        # Create evenly spaced strings across the height
        step = height / (expected_count + 1)
        synthetic_strings = []

        for i in range(1, expected_count + 1):
            rho = i * step
            theta = np.pi / 2  # Horizontal line
            synthetic_strings.append(np.array([[rho, theta]]))

        return np.array(synthetic_strings)

    # Sort by rho value (position)
    strings_sorted = sorted(strings, key=lambda line: line[0][0])

    # Extract rho values
    rhos = [line[0][0] for line in strings_sorted]
    thetas = [line[0][1] for line in strings_sorted]

    # Calculate average theta
    avg_theta = sum(thetas) / len(thetas)

    # If only one string detected, estimate spacing based on image height
    if len(strings) == 1:
        if height is None:
            return strings

        rho = rhos[0]
        # Estimate position in the string set (middle string?)
        position = 3
        step = height / (expected_count + 1)

        synthetic_strings = []
        for i in range(1, expected_count + 1):
            new_rho = i * step
            synthetic_strings.append(np.array([[new_rho, avg_theta]]))

        return np.array(synthetic_strings)

    # Multiple strings detected, interpolate between them
    min_rho, max_rho = min(rhos), max(rhos)

    # Estimate spacing for missing strings
    if height is not None:
        # Use image height as a constraint
        total_span = height
    else:
        # Use detected string span
        total_span = max_rho - min_rho

    step = total_span / (expected_count - 1)

    # Generate new strings at regular intervals
    synthetic_strings = []
    for i in range(expected_count):
        new_rho = min_rho + i * step
        synthetic_strings.append(np.array([[new_rho, avg_theta]]))

    return np.array(synthetic_strings)


def optimize_fret_detection(frets, expected_min=3, width=None):
    """
    Optimize fret detection results.

    Args:
        frets: Detected vertical lines (frets)
        expected_min: Minimum expected number of frets
        width: Width of the image

    Returns:
        Optimized array of fret lines
    """
    if frets is None or len(frets) == 0:
        return generate_synthetic_frets(width) if width is not None else None

    # If we have fewer than minimum expected, try to extrapolate
    if len(frets) < expected_min:
        return extrapolate_frets(frets, expected_min, width)

    # Sort by rho value (x-position)
    frets_sorted = sorted(frets, key=lambda line: abs(line[0][0]))

    return np.array(frets_sorted)


def extrapolate_frets(frets, expected_min, width):
    """
    Extrapolate frets when fewer than expected are detected.

    Frets follow a logarithmic spacing pattern based on the 12-tone equal temperament system.
    Each fret position is calculated as a fraction of the previous fret-to-bridge distance.

    Args:
        frets: Detected fret lines
        expected_min: Minimum expected number of frets
        width: Width of the image

    Returns:
        Array with extrapolated frets
    """
    if frets is None or len(frets) == 0:
        return generate_synthetic_frets(width) if width is not None else None

    # Sort by absolute rho value (position)
    frets_sorted = sorted(frets, key=lambda line: abs(line[0][0]))

    # Extract rho values (x positions)
    rhos = [line[0][0] for line in frets_sorted]
    thetas = [line[0][1] for line in frets_sorted]

    # Calculate average theta
    avg_theta = sum(thetas) / len(thetas)

    # If only one fret detected, cannot extrapolate accurately
    if len(frets) < 2:
        if width is None:
            return frets

        # Generate synthetic frets
        return generate_synthetic_frets(width)

    # For multiple frets, try to estimate the pattern
    # In guitars, fret spacing follows a logarithmic pattern
    # The ratio between consecutive fret distances is approximately 0.944

    # If we have at least two frets, we can estimate the scale length
    positions = sorted([abs(rho) for rho in rhos])

    # Direction might be inverted, check direction
    increasing = positions[-1] > positions[0]

    # Calculate scale length based on first two frets
    # Using 12-tone equal temperament formula
    fret_ratio = 0.944  # Approximate fret distance ratio

    extrapolated_frets = []

    # Add detected frets first
    for i, fret in enumerate(frets_sorted):
        extrapolated_frets.append(fret)

    # Need to extrapolate in both directions

    # Extrapolate toward nut (left/start)
    if len(positions) >= 2:
        f1, f2 = positions[:2]
        distance = f2 - f1

        # Extrapolate to the left/beginning
        next_pos = f1 - distance / fret_ratio

        while next_pos > 0 and len(extrapolated_frets) < expected_min:
            # Add a new fret
            extrapolated_frets.append(np.array([[next_pos if increasing else -next_pos, avg_theta]]))

            # Calculate next position
            distance = distance / fret_ratio
            next_pos = next_pos - distance

    # Extrapolate toward bridge (right/end)
    if len(positions) >= 2:
        f1, f2 = positions[-2:]
        distance = f2 - f1

        # Extrapolate to the right/end
        next_pos = f2 + distance * fret_ratio

        while next_pos < (width if width is not None else 10000) and len(extrapolated_frets) < expected_min:
            # Add a new fret
            extrapolated_frets.append(np.array([[next_pos if increasing else -next_pos, avg_theta]]))

            # Calculate next position
            distance = distance * fret_ratio
            next_pos = next_pos + distance

    # Sort the extrapolated frets
    extrapolated_frets = sorted(extrapolated_frets, key=lambda line: abs(line[0][0]))

    return np.array(extrapolated_frets)


def generate_synthetic_frets(width, fret_count=12):
    """
    Generate synthetic frets based on standard guitar dimensions.

    Args:
        width: Width of the neck image
        fret_count: Number of frets to generate

    Returns:
        Array of synthetic fret lines
    """
    if width is None:
        return None

    # Assume the width covers 12 frets
    scale_length = width * 1.2  # Estimate scale length

    # 12-tone equal temperament formula for fret positions
    fret_positions = []
    for i in range(1, fret_count + 1):
        # Calculate position from nut
        position = scale_length * (1 - 1 / (2 ** (i / 12)))

        # Adjust to make nut at position 0
        # This gives position from left edge of image
        adjusted_position = position

        # Create fret line with vertical orientation
        theta = 0  # Vertical line
        fret_positions.append(np.array([[adjusted_position, theta]]))

    return np.array(fret_positions)


def create_virtual_fretboard(strings, frets, image_shape, raw_detections=None,
                             string_angle=None, fret_angle=None, save_debug=False, max_frets=24):
    """
    Create a virtual fretboard model.

    Args:
        strings: Detected string lines
        frets: Detected fret lines
        image_shape: Shape of the original image (height, width)
        raw_detections: Raw detection points from YOLO
        string_angle: Optional angle for strings (degrees)
        fret_angle: Optional angle for frets (degrees)
        save_debug: Whether to save debug images
        max_frets: Maximum number of frets to include

    Returns:
        Dictionary with virtual fretboard model
    """
    try:
        height, width = image_shape[:2]

        # 각도가 제공되지 않은 경우 기본값 사용
        if string_angle is None:
            string_angle = 5.0  # 기본 문자열 기울기
        if fret_angle is None:
            fret_angle = 95.0  # 기본 프렛 기울기

        print(f"Creating virtual fretboard with angles - string: {string_angle:.2f}°, fret: {fret_angle:.2f}°")

        # 기울기 계산
        string_slope = np.tan(np.radians(string_angle))
        fret_slope = np.tan(np.radians(fret_angle - 90))

        # 문자열 엔드포인트 생성
        string_endpoints = []
        string_positions = []

        if raw_detections and 'string_points' in raw_detections and raw_detections['string_points']:
            for i, points in enumerate(raw_detections['string_points']):
                try:
                    # 중심점 계산
                    x1, y1 = points[0]
                    x2, y2 = points[1]
                    y_center = (y1 + y2) / 2

                    # 기울기 적용
                    new_x1 = 0
                    new_y1 = int(y_center - string_slope * x1)

                    new_x2 = width - 1
                    new_y2 = int(y_center + string_slope * (new_x2 - x1))

                    # 이미지 경계로 클리핑
                    new_y1 = max(0, min(height - 1, new_y1))
                    new_y2 = max(0, min(height - 1, new_y2))

                    p1 = (int(new_x1), int(new_y1))
                    p2 = (int(new_x2), int(new_y2))

                    string_endpoints.append((p1, p2))
                    string_positions.append(int(y_center))
                except Exception as e:
                    print(f"Error processing string {i}: {e}")

        # 프렛 엔드포인트 생성
        fret_endpoints = []
        fret_positions = []

        if raw_detections and 'fret_points' in raw_detections and raw_detections['fret_points']:
            for i, points in enumerate(raw_detections['fret_points']):
                try:
                    # 중심점 계산
                    x1, y1 = points[0]
                    x2, y2 = points[1]
                    x_center = (x1 + x2) / 2

                    # 기울기 적용
                    new_y1 = 0
                    new_x1 = int(x_center - fret_slope * new_y1)

                    new_y2 = height - 1
                    new_x2 = int(x_center + fret_slope * (new_y2 - new_y1))

                    # 이미지 경계로 클리핑
                    new_x1 = max(0, min(width - 1, new_x1))
                    new_x2 = max(0, min(width - 1, new_x2))

                    p1 = (int(new_x1), int(new_y1))
                    p2 = (int(new_x2), int(new_y2))

                    fret_endpoints.append((p1, p2))
                    fret_positions.append(int(x_center))
                except Exception as e:
                    print(f"Error processing fret {i}: {e}")

        # 정렬
        string_positions.sort()
        fret_positions.sort()

        # 프렛보드 모델 생성
        virtual_fretboard = {
            'string_lines': strings,
            'fret_lines': frets,
            'string_positions': string_positions,
            'fret_positions': fret_positions,
            'string_endpoints': string_endpoints,
            'fret_endpoints': fret_endpoints,
            'string_angle': string_angle,
            'fret_angle': fret_angle,
            'image_shape': image_shape
        }

        # 디버그 시각화
        if save_debug:
            import cv2 as cv
            import os

            # 빈 이미지 생성
            debug_image = np.zeros((height, width, 3), dtype=np.uint8)

            # 문자열 그리기
            for i, (p1, p2) in enumerate(string_endpoints):
                cv.line(debug_image, p1, p2, (0, 255, 0), 2)  # 녹색
                cv.putText(debug_image, f"S{i}", p1, cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 0), 1)

            # 프렛 그리기
            for i, (p1, p2) in enumerate(fret_endpoints):
                cv.line(debug_image, p1, p2, (0, 0, 255), 2)  # 빨강
                cv.putText(debug_image, f"F{i}", p1, cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 255), 1)

            # 각도 정보 표시
            cv.putText(debug_image, f"String: {string_angle:.1f}°", (10, 20),
                       cv.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
            cv.putText(debug_image, f"Fret: {fret_angle:.1f}°", (10, 50),
                       cv.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)

            # 이미지 저장
            os.makedirs("temp", exist_ok=True)
            cv.imwrite("temp/virtual_fretboard.jpg", debug_image)

        return virtual_fretboard

    except Exception as e:
        import traceback
        print(f"Error creating virtual fretboard: {e}")
        print(traceback.format_exc())

        # 최소한의 결과 반환
        return {
            'string_lines': strings,
            'fret_lines': frets,
            'string_positions': [],
            'fret_positions': [],
            'string_endpoints': [],
            'fret_endpoints': [],
            'string_angle': 0,
            'fret_angle': 90,
            'image_shape': image_shape
        }


def filter_lines_by_angle(lines, angle_tolerance=10):
    """
    Filter lines to keep only those with similar angles.

    Args:
        lines: Array of lines from HoughLines
        angle_tolerance: Maximum angle difference in degrees

    Returns:
        Filtered array of lines
    """
    if lines is None or len(lines) <= 3:
        return lines

    # 각도들을 추출
    angles = []
    for line in lines:
        _, theta = line[0]
        angle_deg = (theta * 180 / np.pi) - 90
        angles.append(angle_deg)

    # 중앙값 각도 계산
    median_angle = np.median(angles)

    # 중앙값과 비슷한 각도의 선들만 유지
    filtered_lines = []
    for i, line in enumerate(lines):
        if abs(angles[i] - median_angle) <= angle_tolerance:
            filtered_lines.append(line)

    return np.array(filtered_lines)


def is_valid_fretboard(fretboard):
    """
    Check if a detected fretboard is valid.

    Args:
        fretboard: Virtual fretboard model

    Returns:
        True if the fretboard is valid, False otherwise
    """
    if fretboard is None:
        return False

    # Check if we have enough strings
    if 'string_positions' not in fretboard or len(fretboard['string_positions']) < 4:
        return False

    # Check if we have enough frets
    if 'fret_positions' not in fretboard or len(fretboard['fret_positions']) < 3:
        return False

    return True


def fretboard_position_to_pixel(string_idx, fret_idx, fretboard):
    """
    Convert fretboard position (string, fret) to pixel coordinates.

    Args:
        string_idx: String index (0-5 for standard guitar)
        fret_idx: Fret index (0 is open string, 1 is first fret, etc.)
        fretboard: Virtual fretboard model

    Returns:
        Tuple (x, y) representing the pixel position
    """
    if not is_valid_fretboard(fretboard):
        return None

    string_positions = fretboard['string_positions']
    fret_positions = fretboard['fret_positions']

    if string_idx < 0 or string_idx >= len(string_positions):
        return None

    if fret_idx < 0 or fret_idx >= len(fret_positions):
        return None

    y = string_positions[string_idx]
    x = fret_positions[fret_idx]

    return (x, y)


def visualize_fretboard(image, fretboard, finger_positions=None):
    """
    Visualize fretboard with optional finger positions.

    Args:
        image: Original image
        fretboard: Virtual fretboard model
        finger_positions: Optional dictionary mapping fingers to (string, fret) positions

    Returns:
        Visualized image
    """
    if image is None or not is_valid_fretboard(fretboard):
        return image if image is not None else None

    visualization = image.copy()
    height, width = visualization.shape[:2]

    # Draw strings
    for y in fretboard['string_positions']:
        cv.line(visualization, (0, y), (width, y), (0, 255, 0), 1)

    # Draw frets
    for x in fretboard['fret_positions']:
        cv.line(visualization, (x, 0), (x, height), (0, 0, 255), 1)

    # Draw finger positions if provided
    if finger_positions:
        for finger, (string_idx, fret_idx) in finger_positions.items():
            position = fretboard_position_to_pixel(string_idx, fret_idx, fretboard)
            if position:
                x, y = position
                cv.circle(visualization, (x, y), 10, (255, 0, 255), -1)
                cv.putText(visualization, finger[:1], (x - 3, y + 3),
                          cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

    return visualization


def line_intersection(line1_p1, line1_p2, line2_p1, line2_p2):
    """
    Find intersection point of two lines defined by their endpoints.

    Args:
        line1_p1, line1_p2: Endpoints of first line
        line2_p1, line2_p2: Endpoints of second line

    Returns:
        (x, y) intersection point or None if lines are parallel
    """
    # Convert to numpy arrays for calculation
    p1 = np.array(line1_p1)
    p2 = np.array(line1_p2)
    p3 = np.array(line2_p1)
    p4 = np.array(line2_p2)

    # Line segments as vectors
    v1 = p2 - p1
    v2 = p4 - p3

    # Cross product to test for parallel lines
    cross_product = np.cross(v1, v2)

    if abs(cross_product) < 1e-10:  # Parallel or coincident lines
        return None

    # Compute intersection using determinants
    # See: https://en.wikipedia.org/wiki/Line–line_intersection
    x1, y1 = p1
    x2, y2 = p2
    x3, y3 = p3
    x4, y4 = p4

    d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
    if abs(d) < 1e-10:
        return None

    px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d
    py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d

    # Check if intersection is within both line segments
    if (min(x1, x2) <= px <= max(x1, x2) and
            min(y1, y2) <= py <= max(y1, y2) and
            min(x3, x4) <= px <= max(x3, x4) and
            min(y3, y4) <= py <= max(y3, y4)):
        return (px, py)

    return None


def calculate_line_angle(points):
    """
    여러 점들로부터 최적의 직선 피팅 및 각도 계산

    Args:
        points: 선 위의 점들의 리스트 [(x1, y1), (x2, y2), ...]

    Returns:
        (angle, slope): 라디안 각도와 기울기
    """
    import numpy as np

    # 최소 2개 이상의 점이 필요
    if len(points) < 2:
        return 0, 0

    x = [p[0] for p in points]
    y = [p[1] for p in points]

    try:
        # 최소제곱법으로 직선 피팅
        coeffs = np.polyfit(x, y, 1)
        slope = coeffs[0]
        angle = np.arctan(slope)
        return angle, slope
    except Exception as e:
        print(f"Error in line fitting: {e}")
        return 0, 0


def analyze_neck_orientation(neck_crop, neck_position=None):
    """
    넥 이미지에서 기울기를 분석합니다.

    Args:
        neck_crop: YOLO로 크롭된 넥 이미지
        neck_position: 원본 이미지에서의 넥 위치 (x1, y1, x2, y2)

    Returns:
        (string_angle, fret_angle): 문자열과 프렛의 기울기 각도
    """
    import cv2 as cv
    import numpy as np
    import logging

    logger = logging.getLogger("fretboard-detection")

    # 넥 바운딩 박스의 정보로 기본 각도 추정
    if neck_position is not None:
        x1, y1, x2, y2 = neck_position
        width = x2 - x1
        height = y2 - y1
        aspect_ratio = width / height if height > 0 else 1.0

        # 넥의 가로세로 비율로 대략적인 각도 추정
        box_angle = np.arctan2(y2 - y1, x2 - x1) * 180 / np.pi
        logger.info(f"Neck box angle: {box_angle:.2f}°, aspect ratio: {aspect_ratio:.2f}")

        # 기본 각도 설정
        base_string_angle = 5.0 if aspect_ratio > 2.0 else box_angle
        base_fret_angle = 95.0 if aspect_ratio > 2.0 else box_angle + 90
    else:
        # 기본값
        base_string_angle = 5.0  # 약간 아래로 기울어짐
        base_fret_angle = 95.0  # 약간 오른쪽으로 기울어짐

    try:
        # 넥 이미지에서 엣지 검출
        gray = cv.cvtColor(neck_crop, cv.COLOR_BGR2GRAY)
        edges = cv.Canny(gray, 50, 150)

        # 선 감지 (더 많은 선을 감지하기 위해 낮은 임계값 사용)
        lines = cv.HoughLinesP(edges, 1, np.pi / 180, threshold=30,
                               minLineLength=30, maxLineGap=10)

        if lines is None or len(lines) < 3:
            logger.info("Not enough lines detected, using base angles")
            return base_string_angle, base_fret_angle

        # 감지된 선 각도 분석
        angles = []
        for line in lines:
            x1, y1, x2, y2 = line[0]
            # 너무 짧은 선 무시
            length = np.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)
            if length < 20:
                continue

            if x2 == x1:  # 수직선 방지 (arctan 무한대)
                angle = 90
            else:
                angle = np.arctan2(y2 - y1, x2 - x1) * 180 / np.pi

            # 각도 정규화 (-90 ~ 90)
            if angle > 90:
                angle -= 180
            elif angle < -90:
                angle += 180

            angles.append(angle)

        if not angles:
            logger.info("No valid angles found, using base angles")
            return base_string_angle, base_fret_angle

        # 각도 히스토그램으로 주요 방향 결정
        hist, bins = np.histogram(angles, bins=36, range=(-90, 90))
        main_direction_idx = np.argmax(hist)
        main_angle = (bins[main_direction_idx] + bins[main_direction_idx + 1]) / 2

        # 주 방향이 수평에 가까운지 수직에 가까운지 확인
        if abs(main_angle) < 45:  # 수평에 가까움
            string_angle = main_angle
            fret_angle = 90 + main_angle  # 수직 방향 (90도 + 편차)
        else:  # 수직에 가까움
            fret_angle = main_angle
            string_angle = main_angle - 90  # 수평 방향 (수직 - 90도)

        logger.info(f"Analyzed angles - string: {string_angle:.2f}°, fret: {fret_angle:.2f}°")

        # 디버그 이미지 생성
        debug_img = neck_crop.copy()

        # 감지된 모든 선 그리기
        for line in lines:
            x1, y1, x2, y2 = line[0]
            cv.line(debug_img, (x1, y1), (x2, y2), (255, 0, 255), 1)

        # 주요 방향 표시
        h, w = neck_crop.shape[:2]
        center_x, center_y = w // 2, h // 2

        # 문자열 방향 표시
        end_x = int(center_x + np.cos(np.radians(string_angle)) * 50)
        end_y = int(center_y + np.sin(np.radians(string_angle)) * 50)
        cv.line(debug_img, (center_x, center_y), (end_x, end_y), (0, 255, 0), 2)

        # 프렛 방향 표시
        end_x = int(center_x + np.cos(np.radians(fret_angle)) * 50)
        end_y = int(center_y + np.sin(np.radians(fret_angle)) * 50)
        cv.line(debug_img, (center_x, center_y), (end_x, end_y), (0, 0, 255), 2)

        # 텍스트 추가
        cv.putText(debug_img, f"String: {string_angle:.1f}°", (10, 20),
                   cv.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)
        cv.putText(debug_img, f"Fret: {fret_angle:.1f}°", (10, 40),
                   cv.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1)

        # 디버그 이미지 저장
        import os
        os.makedirs("temp", exist_ok=True)
        cv.imwrite("temp/neck_orientation.jpg", debug_img)

        return string_angle, fret_angle

    except Exception as e:
        logger.error(f"Error in neck orientation analysis: {e}")
        import traceback
        logger.error(traceback.format_exc())
        return base_string_angle, base_fret_angle

### 테스트용 ###
def test_draw_lines(image):
    """단순 테스트 함수: 대각선 그리기"""
    import cv2 as cv
    import logging
    # 기존 로거 사용
    logger = logging.getLogger('guitar-chord-assistant')

    # 직접 print 추가 (확실한 출력을 위해)
    print("=== TEST DRAW LINES CALLED ===")

    if image is None:
        logger.error("Input image is None")
        return None

    vis_image = image.copy()
    height, width = vis_image.shape[:2]

    logger.info(f"Drawing diagonal lines on image with shape: {vis_image.shape}")

    # 대각선 그리기
    cv.line(vis_image, (0, 0), (width, height), (0, 255, 0), 2)
    cv.line(vis_image, (width, 0), (0, height), (0, 0, 255), 2)

    logger.info("Lines drawn successfully")
    return vis_image
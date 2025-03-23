"""
Geometric transformations and line detection for guitar chord recognition.

This module provides functions for detecting guitar strings using Hough transform,
correcting image angle, and handling perspective transformations.
"""

import math
import os
import cv2 as cv
import numpy as np
import torch

# Import from our refactored modules
from src.processing.image_processing import frei_and_chen_edges

# Default temporary directory for debug images
TMP_DIR = os.path.join("temp", "")


def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


def line_points(line, offset):
    """
    Calculate endpoints of a line represented in rho-theta form.

    Args:
        line: Line in rho-theta format [rho, theta]
        offset: Length to extend the line in both directions

    Returns:
        Tuple of two points ((x1, y1), (x2, y2))
    """
    rho, theta = line[0]
    a = np.cos(theta)
    b = np.sin(theta)
    x0 = a * rho
    y0 = b * rho
    x1 = int(x0 + offset * -b)
    y1 = int(y0 + offset * a)
    x2 = int(x0 - offset * -b)
    y2 = int(y0 - offset * a)
    return (x1, y1), (x2, y2)


def angle_between(p1, p2):
    """
    Calculate angle between two points in degrees.

    Args:
        p1: First point (x, y)
        p2: Second point (x, y)

    Returns:
        Angle in degrees
    """
    ang1 = np.arctan2(*p1[::-1])
    ang2 = np.arctan2(*p2[::-1])
    return np.rad2deg((ang1 - ang2) % (2 * np.pi))


def angle_axis_x(line):
    """
    Calculate angle between a line and the x-axis.

    Args:
        line: Line in rho-theta format [rho, theta]

    Returns:
        Angle in degrees
    """
    rho, theta = line[0]
    # Converting to degrees
    theta = theta * 180 / np.pi
    angle = theta - 90  # Real angle with respect to x axis
    return angle


def filter_lines(lines, offset=30):
    """
    Filter out lines that are too vertical (perpendicular to x-axis).

    Args:
        lines: Array of lines in rho-theta format
        offset: Angular offset in degrees for filtering

    Returns:
        Filtered array of lines or None
    """
    if lines is None:
        return lines

    # Define exclusion intervals (lines too close to vertical)
    exclude_interval_1 = [-90 - offset, -90 + offset]
    exclude_interval_2 = [90 - offset, 90 + offset]

    filtered_lines = None
    for l in lines:
        angle = angle_axis_x(l)

        # Skip lines that are too vertical
        if (exclude_interval_1[0] <= angle <= exclude_interval_1[1] or
            exclude_interval_2[0] <= angle <= exclude_interval_2[1]):
            continue
        else:
            if filtered_lines is None:
                filtered_lines = np.copy(l)
                filtered_lines = filtered_lines[np.newaxis, :]
            else:
                filtered_lines = np.concatenate(
                    (filtered_lines, l[np.newaxis, :]), axis=0)

    return filtered_lines


def detect_lines(image, threshold=270, max_attempts=30, min_threshold=20, verbose=False):
    """
    Detect lines using Hough transform with adaptive thresholding.

    Args:
        image: Input image (numpy array)
        threshold: Initial threshold for Hough transform
        max_attempts: Maximum number of attempts with different thresholds
        min_threshold: Minimum acceptable threshold
        verbose: If True, print detailed information

    Returns:
        Tuple of (detected lines, final threshold used)
    """
    # Convert to edges
    edges = frei_and_chen_edges(torch.from_numpy(image)).numpy()

    # Initial line detection
    lines = cv.HoughLines(edges, 1, np.pi / 180, threshold)

    # Filter lines (remove vertical ones)
    lines = filter_lines(lines)

    # If no lines found, try with lower thresholds
    if lines is None:
        for i in range(1, max_attempts + 1):
            threshold = threshold - 20
            if threshold <= min_threshold:
                if verbose:
                    print(f"No lines found after {i-1} attempts with threshold {threshold+20}.")
                return None, threshold

            lines = cv.HoughLines(edges, 1, np.pi / 180, threshold)
            lines = filter_lines(lines)

            if lines is not None:
                break

    # If too many lines, try with higher thresholds
    if lines is not None and len(lines) > 6:
        optimized_threshold = threshold
        optimized_lines = None

        for i in range(1, max_attempts + 1):
            optimized_threshold = optimized_threshold + 20
            optimized_lines = cv.HoughLines(edges, 1, np.pi / 180, optimized_threshold)
            optimized_lines = filter_lines(optimized_lines)

            if optimized_lines is not None and len(optimized_lines) <= 6:
                lines = optimized_lines
                threshold = optimized_threshold
                break

    if verbose and lines is not None:
        print(f"{len(lines)} lines found with threshold {threshold}")

    return lines, threshold


def draw_detected_lines(image, lines):
    """
    Draw detected lines on an image.

    Args:
        image: Input image (numpy array)
        lines: Array of lines in rho-theta format

    Returns:
        Image with drawn lines
    """
    if lines is None:
        return image

    result = np.copy(image)
    max_l = math.sqrt(image.shape[0]**2 + image.shape[1]**2)

    for line in lines:
        p1, p2 = line_points(line, offset=max_l)
        cv.line(result, p1, p2, (0, 0, 255), 2)

    return result


def correct_angle(img, threshold=270, verbose=False, save_images=False):
    """
    Correct image orientation by detecting lines and rotating to align with x-axis.

    This function detects guitar strings using Hough transform and rotates
    the image to make strings parallel to the x-axis.

    Args:
        img: Input image (BGR PyTorch tensor)
        threshold: Threshold for line detection
        verbose: If True, print detailed information
        save_images: If True, save intermediate results for debugging

    Returns:
        Angle-corrected image (BGR PyTorch tensor)
    """
    # Ensure temp directory exists if saving images
    if save_images:
        ensure_temp_dir()

    # Convert to numpy for OpenCV processing
    img_np = img.numpy()

    # Detect lines
    lines, threshold = detect_lines(img_np, threshold, verbose=verbose)

    # If no lines detected, return original image
    if lines is None or len(lines) == 0:
        return img

    # Draw lines for visualization
    if save_images:
        lined_img = draw_detected_lines(img_np, lines)
        cv.imwrite(os.path.join(TMP_DIR, 'houghlines.jpg'), lined_img)

    # Calculate rotation angle (median of all line angles)
    median_theta = np.median(lines[:, :, 1])
    median_theta_deg = median_theta * 180 / np.pi
    angle = median_theta_deg - 90  # Angle to rotate

    # Rotate image
    h, w = img_np.shape[:2]
    center = (w / 2, h / 2)
    rotation_matrix = cv.getRotationMatrix2D(center, angle, 1.0)
    rotated = cv.warpAffine(img_np, rotation_matrix, (w, h))

    if save_images:
        cv.imwrite(os.path.join(TMP_DIR, 'rotated_with_black_borders.jpg'), rotated)

    # Crop to remove black borders
    rotated_cropped = crop_around_center(
        rotated,
        *largest_rotated_rect(w, h, math.radians(angle))
    )

    if save_images:
        cv.imwrite(os.path.join(TMP_DIR, 'rotated.jpg'), rotated_cropped)

    # Convert back to PyTorch tensor
    return torch.from_numpy(rotated_cropped)


def largest_rotated_rect(w, h, angle):
    """
    Calculate largest inscribed axis-aligned rectangle within a rotated rectangle.

    Args:
        w: Original width
        h: Original height
        angle: Rotation angle in radians

    Returns:
        Tuple of (width, height) for the largest inscribed rectangle
    """
    quadrant = int(math.floor(angle / (math.pi / 2))) & 3
    sign_alpha = angle if ((quadrant & 1) == 0) else math.pi - angle
    alpha = (sign_alpha % math.pi + math.pi) % math.pi

    bb_w = w * math.cos(alpha) + h * math.sin(alpha)
    bb_h = w * math.sin(alpha) + h * math.cos(alpha)

    gamma = math.atan2(bb_w, bb_w) if (w < h) else math.atan2(bb_w, bb_w)

    delta = math.pi - alpha - gamma

    length = h if (w < h) else w

    d = length * math.cos(alpha)
    a = d * math.sin(alpha) / math.sin(delta)

    y = a * math.cos(gamma)
    x = y * math.tan(gamma)

    return (bb_w - 2 * x, bb_h - 2 * y)


def crop_around_center(image, width, height):
    """
    Crop an image to specified width and height around its center.

    Args:
        image: Input image (numpy array)
        width: Target width
        height: Target height

    Returns:
        Cropped image
    """
    image_size = (image.shape[1], image.shape[0])
    image_center = (int(image_size[0] * 0.5), int(image_size[1] * 0.5))

    # Ensure dimensions don't exceed image bounds
    width = min(width, image_size[0])
    height = min(height, image_size[1])

    # Calculate crop coordinates
    x1 = int(image_center[0] - width * 0.5)
    x2 = int(image_center[0] + width * 0.5)
    y1 = int(image_center[1] - height * 0.5)
    y2 = int(image_center[1] + height * 0.5)

    return image[y1:y2, x1:x2]


def detect_strings_and_frets(image, string_threshold=250, fret_threshold=200, verbose=False):
    """
    Detect guitar strings and frets in an image.

    Args:
        image: Input image (numpy array or PyTorch tensor)
        string_threshold: Threshold for string detection
        fret_threshold: Threshold for fret detection
        verbose: If True, print detailed information

    Returns:
        Tuple of (strings, frets) where each is an array of lines in rho-theta format
    """
    # Ensure image is in numpy format
    if isinstance(image, torch.Tensor):
        image = image.numpy()

    # Convert to edges
    edges = frei_and_chen_edges(torch.from_numpy(image)).numpy()

    # Detect horizontal lines (strings)
    lines = cv.HoughLines(edges, 1, np.pi / 180, string_threshold)
    strings = filter_lines(lines, offset=30)

    # Detect vertical lines (frets) - use different filter parameters
    lines = cv.HoughLines(edges, 1, np.pi / 180, fret_threshold)

    # For frets, we want to keep the vertical lines, so we invert the filter logic
    if lines is not None:
        frets = []
        for line in lines:
            angle = angle_axis_x(line)
            # Keep lines that are close to vertical
            if (-90-30 <= angle <= -90+30) or (90-30 <= angle <= 90+30):
                frets.append(line)

        frets = np.array(frets) if frets else None
    else:
        frets = None

    if verbose:
        if strings is not None:
            print(f"Detected {len(strings)} strings")
        if frets is not None:
            print(f"Detected {len(frets)} frets")

    return strings, frets
"""
Hand tracking module using MediaPipe.

This module provides functions to track hand landmarks using MediaPipe Hands,
and map them to fretboard positions for chord recognition.
"""

import os
import cv2 as cv
import numpy as np
import mediapipe as mp
import math
from typing import Dict, List, Tuple, Optional, Any

# Initialize MediaPipe solutions
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

# Default temporary directory for debug images
TMP_DIR = os.path.join("temp", "")


def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


def setup_hand_tracking(
        static_mode=False,
        max_hands=2,
        min_detection_confidence=0.7,
        min_tracking_confidence=0.5
):
    """
    Setup MediaPipe hand tracking.

    Args:
        static_mode: Whether to treat input images as a video stream or static images
        max_hands: Maximum number of hands to detect
        min_detection_confidence: Minimum confidence for hand detection to be successful
        min_tracking_confidence: Minimum confidence for landmark tracking to continue

    Returns:
        MediaPipe Hands object
    """
    return mp_hands.Hands(
        static_image_mode=static_mode,
        max_num_hands=max_hands,
        min_detection_confidence=min_detection_confidence,
        min_tracking_confidence=min_tracking_confidence
    )


def track_hand(image, hands_model=None, save_debug=False):
    """
    Track hand landmarks in an image.

    Args:
        image: Input image (BGR format)
        hands_model: Pre-configured MediaPipe Hands model
        save_debug: Whether to save debug visualization

    Returns:
        Dictionary with hand landmarks and handedness information or None if no hands detected
    """
    # Create a new model if not provided
    if hands_model is None:
        hands_model = setup_hand_tracking()

    # Create temp directory if debug is enabled
    if save_debug:
        ensure_temp_dir()

    # Convert BGR image to RGB
    image_rgb = cv.cvtColor(image, cv.COLOR_BGR2RGB)

    # Process the image to find hand landmarks
    results = hands_model.process(image_rgb)

    # If no hands detected, return None
    if not results.multi_hand_landmarks:
        return None

    # Save debug visualization if enabled
    if save_debug:
        debug_image = image.copy()
        for hand_landmarks in results.multi_hand_landmarks:
            mp_drawing.draw_landmarks(
                debug_image,
                hand_landmarks,
                mp_hands.HAND_CONNECTIONS,
                mp_drawing_styles.get_default_hand_landmarks_style(),
                mp_drawing_styles.get_default_hand_connections_style()
            )
        cv.imwrite(os.path.join(TMP_DIR, "hand_landmarks.jpg"), debug_image)

    # Return both landmarks and handedness info
    return {
        'landmarks': results.multi_hand_landmarks,
        'handedness': results.multi_handedness
    }


def get_hand_landmarks_by_type(hand_results, hand_type="Left"):
    """
    Get hand landmarks by specified hand type (Left or Right).

    For guitar playing, left hand is typically on the fretboard.

    Args:
        hand_results: Results from track_hand function
        hand_type: "Left" or "Right" hand to select

    Returns:
        Selected hand landmarks or None if not found
    """
    if hand_results is None or 'landmarks' not in hand_results or 'handedness' not in hand_results:
        return None

    # Find the specified hand type
    for i, handedness in enumerate(hand_results['handedness']):
        # MediaPipe returns handedness as a list of classifications
        if handedness.classification[0].label == hand_type:
            return hand_results['landmarks'][i]

    # If no matching hand found, return the first one as fallback
    if hand_results['landmarks'] and len(hand_results['landmarks']) > 0:
        return hand_results['landmarks'][0]

    return None


def get_fingertip_positions(hand_landmarks, image_shape):
    """
    Extract fingertip positions from hand landmarks.

    Args:
        hand_landmarks: MediaPipe hand landmarks
        image_shape: Shape of the image (height, width)

    Returns:
        Dictionary with fingertip positions {finger_name: (x, y)}
    """
    if hand_landmarks is None:
        return {}

    height, width = image_shape[:2]

    # Fingertip landmark indices in MediaPipe hand model
    fingertip_indices = {
        "thumb": 4,
        "index": 8,
        "middle": 12,
        "ring": 16,
        "pinky": 20
    }

    # Also track knuckles (proximal phalanx start) for better chord analysis
    knuckle_indices = {
        "thumb_knuckle": 2,  # proximal phalanx
        "index_knuckle": 5,
        "middle_knuckle": 9,
        "ring_knuckle": 13,
        "pinky_knuckle": 17
    }

    fingertips = {}
    # Get fingertip positions
    for finger_name, idx in fingertip_indices.items():
        landmark = hand_landmarks.landmark[idx]
        fingertips[finger_name] = (int(landmark.x * width), int(landmark.y * height))

    # Get knuckle positions
    for knuckle_name, idx in knuckle_indices.items():
        landmark = hand_landmarks.landmark[idx]
        fingertips[knuckle_name] = (int(landmark.x * width), int(landmark.y * height))

    return fingertips


def find_nearest_string(point, fretboard):
    """
    Find the nearest string to a given point.

    Args:
        point: (x, y) coordinates
        fretboard: Virtual fretboard model with string positions

    Returns:
        Index of the nearest string
    """
    if 'string_positions' not in fretboard or not fretboard['string_positions']:
        return -1

    # Calculate distance to each string (y-coordinate)
    y = point[1]
    string_positions = fretboard['string_positions']
    distances = [abs(y - string_y) for string_y in string_positions]

    # Find the nearest string
    nearest_idx = distances.index(min(distances))

    # Return the index (0 is highest pitched string, 5 is lowest for standard guitar)
    return nearest_idx


def find_nearest_fret(point, fretboard):
    """
    Find the nearest fret to a given point.

    Args:
        point: (x, y) coordinates
        fretboard: Virtual fretboard model with fret positions

    Returns:
        Index of the nearest fret
    """
    if 'fret_positions' not in fretboard or not fretboard['fret_positions']:
        return -1

    # Calculate distance to each fret (x-coordinate)
    x = point[0]
    fret_positions = fretboard['fret_positions']

    # Add a virtual "open string" position to the left of the first fret
    if len(fret_positions) > 0:
        leftmost_fret = fret_positions[0]
        # Estimate nut position (to the left of first fret)
        nut_position = max(0, leftmost_fret - 50)  # Adjust this offset as needed
        all_positions = [nut_position] + fret_positions
    else:
        all_positions = fret_positions

    # Calculate distances to all positions (including virtual nut)
    distances = [abs(x - fret_x) for fret_x in all_positions]

    # Find the nearest position
    nearest_idx = distances.index(min(distances))

    # Return the index (0 is open string, 1 is first fret, etc.)
    return nearest_idx


def calculate_finger_pressing_probability(fingertip, knuckle, string_y, threshold_angle=30):
    """
    Calculate probability that a finger is actually pressing a string.

    This helps distinguish between fingers that are just hovering over the fretboard
    versus fingers that are actively pressing a string.

    Args:
        fingertip: (x, y) coordinates of fingertip
        knuckle: (x, y) coordinates of the same finger's knuckle
        string_y: y-coordinate of the string
        threshold_angle: angle threshold in degrees

    Returns:
        Probability (0-1) that finger is pressing the string
    """
    # Vector from knuckle to fingertip
    finger_vector = (fingertip[0] - knuckle[0], fingertip[1] - knuckle[1])

    # Vector pointing straight down
    down_vector = (0, 1)

    # Calculate angle between vectors
    dot_product = finger_vector[0] * down_vector[0] + finger_vector[1] * down_vector[1]
    finger_magnitude = math.sqrt(finger_vector[0]**2 + finger_vector[1]**2)
    down_magnitude = math.sqrt(down_vector[0]**2 + down_vector[1]**2)

    # Avoid division by zero
    if finger_magnitude == 0 or down_magnitude == 0:
        return 0.0

    cos_angle = dot_product / (finger_magnitude * down_magnitude)
    angle_rad = math.acos(max(-1, min(1, cos_angle)))  # Clamp to valid range
    angle_deg = math.degrees(angle_rad)

    # Closer to vertical pointing down = higher probability
    pressing_probability = max(0, 1 - (angle_deg / threshold_angle))

    # Additional factor: distance from string
    distance_to_string = abs(fingertip[1] - string_y)
    max_distance = 20  # Pixels
    distance_factor = max(0, 1 - (distance_to_string / max_distance))

    # Combined probability
    return pressing_probability * distance_factor


def map_hand_to_fretboard(hand_results, fretboard, image_shape, min_pressing_prob=0.5):
    """
    Map hand landmarks to fretboard positions.

    Args:
        hand_results: Results from track_hand function
        fretboard: Fretboard model with strings and frets
        image_shape: Shape of the image
        min_pressing_prob: Minimum probability to consider a finger is pressing a string

    Returns:
        Dictionary mapping fingers to (string, fret) positions and probabilities
    """
    # Get left hand landmarks (on fretboard)
    hand_landmarks = get_hand_landmarks_by_type(hand_results, hand_type="Left")

    if hand_landmarks is None:
        return {}

    # Get fingertip and knuckle positions
    finger_positions = get_fingertip_positions(hand_landmarks, image_shape)
    if not finger_positions:
        return {}

    # Map fingers to strings and frets
    finger_mappings = {}
    for finger in ["index", "middle", "ring", "pinky", "thumb"]:
        if finger in finger_positions and f"{finger}_knuckle" in finger_positions:
            fingertip = finger_positions[finger]
            knuckle = finger_positions[f"{finger}_knuckle"]

            # Find nearest string and fret
            string_idx = find_nearest_string(fingertip, fretboard)
            fret_idx = find_nearest_fret(fingertip, fretboard)

            # Skip if string or fret detection failed
            if string_idx == -1 or fret_idx == -1:
                continue

            # Calculate probability that the finger is actually pressing the string
            string_y = fretboard['string_positions'][string_idx] if string_idx < len(fretboard['string_positions']) else 0
            pressing_prob = calculate_finger_pressing_probability(fingertip, knuckle, string_y)

            # Only include fingers that are likely pressing strings
            if pressing_prob >= min_pressing_prob:
                finger_mappings[finger] = {
                    'string': string_idx,
                    'fret': fret_idx,
                    'probability': pressing_prob,
                    'position': fingertip
                }

    return finger_mappings


def analyze_chord_from_finger_positions(finger_mappings):
    """
    Analyze finger positions to determine the chord being played.

    This is a placeholder function - actual chord recognition would require
    a more sophisticated algorithm or database of chord shapes.

    Args:
        finger_mappings: Dictionary of finger positions from map_hand_to_fretboard

    Returns:
        Dictionary with chord name and confidence
    """
    # This is a simplified placeholder implementation
    # A real implementation would compare against a database of chord shapes

    # Convert finger mappings to a chord shape representation
    # Standard guitar has 6 strings, so we use a list of length 6
    # Each element is the fret number, with -1 meaning "not played"
    chord_shape = [-1] * 6  # Default: all strings not played

    for finger, data in finger_mappings.items():
        string_idx = data['string']
        fret_idx = data['fret']
        if 0 <= string_idx < 6:  # Valid string index
            chord_shape[string_idx] = fret_idx

    # TODO: Implement actual chord recognition logic or ML model here
    # For now, just return a placeholder result
    return {
        'chord_name': 'Unknown',
        'confidence': 0.0,
        'shape': chord_shape
    }


def visualize_finger_mapping(image, fretboard, finger_mappings, chord_info=None):
    """
    Visualize finger mappings on the fretboard.

    Args:
        image: Input image
        fretboard: Virtual fretboard model
        finger_mappings: Dictionary from map_hand_to_fretboard
        chord_info: Optional chord recognition results

    Returns:
        Visualization image
    """
    result = image.copy()

    # Draw fretboard
    height, width = result.shape[:2]

    # Draw strings
    string_positions = fretboard.get('string_positions', [])
    for i, y in enumerate(string_positions):
        cv.line(result, (0, y), (width, y), (0, 255, 0), 1)

    # Draw frets
    fret_positions = fretboard.get('fret_positions', [])
    for i, x in enumerate(fret_positions):
        cv.line(result, (x, 0), (x, height), (0, 0, 255), 1)

    # Color mapping for fingers
    finger_colors = {
        "thumb": (255, 0, 0),    # Blue
        "index": (0, 255, 0),    # Green
        "middle": (0, 0, 255),   # Red
        "ring": (255, 255, 0),   # Cyan
        "pinky": (255, 0, 255)   # Magenta
    }

    # Draw finger positions
    for finger, data in finger_mappings.items():
        string_idx = data['string']
        fret_idx = data['fret']
        probability = data['probability']
        position = data['position']

        # Make sure indices are valid
        if 0 <= string_idx < len(string_positions) and 0 <= fret_idx <= len(fret_positions):
            # For fret 0 (open string), use a special marker
            if fret_idx == 0:
                # Draw a circle at the nut position
                nut_x = 10  # Arbitrary position for nut
                string_y = string_positions[string_idx]
                cv.circle(result, (nut_x, string_y), 10, finger_colors.get(finger, (0, 255, 255)), 2)

                # Add label
                cv.putText(result, finger[0].upper(), (nut_x - 5, string_y + 5),
                         cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
            else:
                # Regular fretted note
                # Get actual fret position (subtract 1 since fret_idx 0 is open string)
                fret_x = fret_positions[fret_idx - 1] if fret_idx > 0 and fret_idx <= len(fret_positions) else 0
                string_y = string_positions[string_idx]

                # Draw filled circle with opacity based on probability
                overlay = result.copy()
                cv.circle(overlay, (fret_x, string_y), 12, finger_colors.get(finger, (0, 255, 255)), -1)

                # Apply transparency based on probability
                alpha = min(0.7, max(0.3, probability))  # Limit alpha range
                cv.addWeighted(overlay, alpha, result, 1 - alpha, 0, result)

                # Add finger label
                cv.putText(result, finger[0].upper(), (fret_x - 5, string_y + 5),
                         cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

    # Display chord name if available
    if chord_info and 'chord_name' in chord_info:
        chord_name = chord_info['chord_name']
        confidence = chord_info.get('confidence', 0.0)

        # Create overlay for text background
        overlay = result.copy()
        cv.rectangle(overlay, (10, 10), (200, 50), (0, 0, 0), -1)
        cv.addWeighted(overlay, 0.7, result, 0.3, 0, result)

        # Draw chord name and confidence
        cv.putText(result, f"Chord: {chord_name}", (20, 30),
                 cv.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
        cv.putText(result, f"Confidence: {confidence:.2f}", (20, 50),
                 cv.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1)

    return result
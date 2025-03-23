"""
Visualization and rendering module for guitar chord recognition.

This module provides functions to visualize detection results,
render virtual fretboard, and create user interface elements.
"""

import os
import cv2 as cv
import numpy as np
import matplotlib.pyplot as plt
import mediapipe as mp
from matplotlib.patches import Rectangle

# Default temporary directory
TMP_DIR = os.path.join("temp", "")


def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


def draw_detection_box(image, box, label=None, color=(0, 255, 0), thickness=2):
    """
    Draw a detection bounding box on an image.

    Args:
        image: Input image
        box: Bounding box coordinates (x, y, w, h) or (x1, y1, x2, y2)
        label: Optional text label
        color: Box color as BGR tuple
        thickness: Line thickness

    Returns:
        Image with drawn box
    """
    result = image.copy()

    # Handle different box formats
    if len(box) == 4:
        if box[2] < box[0] or box[3] < box[1]:  # (x1, y1, x2, y2) format
            x1, y1, x2, y2 = box
        else:  # (x, y, w, h) format
            x, y, w, h = box
            x1, y1, x2, y2 = x, y, x + w, y + h

    # Draw rectangle
    cv.rectangle(result, (int(x1), int(y1)), (int(x2), int(y2)), color, thickness)

    # Add label if provided
    if label:
        font = cv.FONT_HERSHEY_SIMPLEX
        font_scale = 0.5
        font_thickness = 1
        text_size = cv.getTextSize(label, font, font_scale, font_thickness)[0]

        # Draw text background
        cv.rectangle(result,
                    (int(x1), int(y1) - text_size[1] - 5),
                    (int(x1) + text_size[0], int(y1)),
                    color, -1)

        # Draw text
        cv.putText(result, label, (int(x1), int(y1) - 5),
                  font, font_scale, (255, 255, 255), font_thickness)

    return result


def visualize_guitar_detection(image, guitar_box=None, neck_box=None, save_path=None):
    """
    Visualize guitar and neck detection results.

    Args:
        image: Input image
        guitar_box: Detected guitar bounding box
        neck_box: Detected neck bounding box
        save_path: Optional path to save the visualization

    Returns:
        Visualization image
    """
    result = image.copy()

    # Draw guitar box if provided
    if guitar_box is not None:
        result = draw_detection_box(result, guitar_box, "Guitar", (0, 165, 255), 2)

    # Draw neck box if provided
    if neck_box is not None:
        result = draw_detection_box(result, neck_box, "Guitar Neck", (0, 255, 0), 2)

    # Save if path provided
    if save_path:
        ensure_temp_dir()
        cv.imwrite(save_path, result)

    return result


def visualize_fretboard(image, fretboard, finger_positions=None, chord_name=None, save_path=None):
    """
    Visualize fretboard with strings, frets, and optional finger positions.

    Args:
        image: Input image
        fretboard: Virtual fretboard model
        finger_positions: Optional dictionary mapping fingers to (string, fret) positions
        chord_name: Optional chord name to display
        save_path: Optional path to save the visualization

    Returns:
        Visualization image
    """
    if image is None or fretboard is None:
        return image

    result = image.copy()
    height, width = result.shape[:2]

    # Draw strings
    string_positions = fretboard.get('string_positions', [])
    for i, y in enumerate(string_positions):
        cv.line(result, (0, y), (width, y), (0, 255, 0), 1)
        # Add string numbers
        cv.putText(result, f"{i+1}", (5, y-5),
                  cv.FONT_HERSHEY_SIMPLEX, 0.4, (255, 255, 255), 1)

    # Draw frets
    fret_positions = fretboard.get('fret_positions', [])
    for i, x in enumerate(fret_positions):
        cv.line(result, (x, 0), (x, height), (0, 0, 255), 1)
        # Add fret numbers
        cv.putText(result, f"{i}", (x+5, 15),
                  cv.FONT_HERSHEY_SIMPLEX, 0.4, (255, 255, 255), 1)

    # Draw finger positions if provided
    if finger_positions and len(string_positions) > 0 and len(fret_positions) > 0:
        for finger, (string_idx, fret_idx) in finger_positions.items():
            if 0 <= string_idx < len(string_positions) and 0 <= fret_idx < len(fret_positions):
                y = string_positions[string_idx]
                x = fret_positions[fret_idx]

                # Draw circle at finger position
                cv.circle(result, (x, y), 10, (255, 0, 255), -1)

                # Draw finger label
                finger_label = finger[:1].upper()  # First letter
                cv.putText(result, finger_label, (x-4, y+4),
                          cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

    # Draw chord name if provided
    if chord_name:
        # Add semi-transparent background
        overlay = result.copy()
        cv.rectangle(overlay, (0, 0), (width, 40), (0, 0, 0), -1)
        cv.addWeighted(overlay, 0.7, result, 0.3, 0, result)

        cv.putText(result, chord_name, (width//2 - 50, 30),
                  cv.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)

    # Save if path provided
    if save_path:
        ensure_temp_dir()
        cv.imwrite(save_path, result)

    return result


def create_chord_diagram(chord_positions, chord_name, save_path=None):
    """
    Create a chord diagram using matplotlib.

    Args:
        chord_positions: Dictionary mapping string indices to fret positions
        chord_name: Name of the chord
        save_path: Optional path to save the diagram

    Returns:
        Matplotlib figure
    """
    # Set up the figure
    fig, ax = plt.subplots(figsize=(4, 6))
    ax.set_xlim(-0.5, 5.5)
    ax.set_ylim(5.5, -0.5)  # Reversed y-axis

    # Number of strings and frets to show
    num_strings = 6
    num_frets = 5

    # Draw strings
    for i in range(num_strings):
        ax.axvline(i, color='black', linewidth=1)

    # Draw frets
    for i in range(num_frets + 1):
        ax.axhline(i, color='black', linewidth=2 if i == 0 else 1)

    # Add string labels (EADGBE)
    string_labels = ['E', 'A', 'D', 'G', 'B', 'E']
    for i, label in enumerate(string_labels):
        ax.text(i, -0.3, label, ha='center', fontsize=12)

    # Add fret numbers
    for i in range(1, num_frets + 1):
        ax.text(-0.3, i, str(i), va='center', fontsize=10)

    # Mark finger positions
    for string_idx, fret_position in chord_positions.items():
        if fret_position > 0:  # Not open string
            ax.add_patch(Rectangle((string_idx - 0.3, fret_position - 0.3), 0.6, 0.6,
                                   facecolor='blue', edgecolor='black'))
        elif fret_position == 0:  # Open string
            ax.plot(string_idx, 0, 'o', markersize=10, mfc='none', mec='black')
        elif fret_position == -1:  # String not played
            ax.text(string_idx, 0, 'X', ha='center', va='center', fontsize=12, fontweight='bold')

    # Add chord name
    ax.text(2.5, -1, chord_name, ha='center', fontsize=16, fontweight='bold')

    # Remove axis ticks and labels
    ax.set_xticks([])
    ax.set_yticks([])
    ax.set_xlabel('')
    ax.set_ylabel('')

    # Adjust layout
    plt.tight_layout()

    # Save if path provided
    if save_path:
        ensure_temp_dir()
        plt.savefig(save_path, dpi=100, bbox_inches='tight')

    return fig


def create_status_bar(image, status_text, progress=None, color=(0, 255, 0)):
    """
    Add a status bar at the bottom of the image.

    Args:
        image: Input image
        status_text: Text to display
        progress: Optional progress value (0-100)
        color: Status bar color

    Returns:
        Image with status bar
    """
    height, width = image.shape[:2]
    result = image.copy()

    # Create status bar area
    bar_height = 30
    status_area = np.zeros((bar_height, width, 3), dtype=np.uint8)

    # Add progress bar if provided
    if progress is not None:
        progress = max(0, min(100, progress))  # Clamp to 0-100
        progress_width = int(width * progress / 100)
        status_area[:, :progress_width] = color

    # Add text
    font = cv.FONT_HERSHEY_SIMPLEX
    text_size = cv.getTextSize(status_text, font, 0.6, 1)[0]
    text_x = (width - text_size[0]) // 2
    text_y = (bar_height + text_size[1]) // 2
    cv.putText(status_area, status_text, (text_x, text_y),
              font, 0.6, (255, 255, 255), 1)

    # Combine with original image
    result = np.vstack((result, status_area))

    return result


def create_chord_feedback(image, target_chord, detected_chord, accuracy, save_path=None):
    """
    Create feedback visualization for chord learning.

    Args:
        image: Input image with fretboard
        target_chord: Target chord name
        detected_chord: Detected chord name
        accuracy: Accuracy score (0-100)
        save_path: Optional path to save the visualization

    Returns:
        Feedback visualization
    """
    height, width = image.shape[:2]
    result = image.copy()

    # Add semi-transparent overlay
    overlay = result.copy()
    cv.rectangle(overlay, (0, 0), (width, 70), (0, 0, 0), -1)
    cv.addWeighted(overlay, 0.7, result, 0.3, 0, result)

    # Add target and detected chord information
    cv.putText(result, f"Target: {target_chord}", (20, 25),
              cv.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 1)

    # Color for detected chord (green if matched, red if not)
    chord_color = (0, 255, 0) if target_chord == detected_chord else (0, 0, 255)
    cv.putText(result, f"Detected: {detected_chord}", (20, 55),
              cv.FONT_HERSHEY_SIMPLEX, 0.7, chord_color, 2)

    # Add accuracy indicator
    accuracy_x = width - 150
    cv.putText(result, f"Accuracy: {accuracy:.1f}%", (accuracy_x, 40),
              cv.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 1)

    # Add progress bar
    bar_width = 120
    bar_height = 10
    bar_x = width - 150
    bar_y = 50

    # Background bar
    cv.rectangle(result, (bar_x, bar_y), (bar_x + bar_width, bar_y + bar_height),
                (100, 100, 100), -1)

    # Progress bar
    progress_width = int(bar_width * accuracy / 100)

    # Color gradient based on accuracy
    if accuracy < 40:
        bar_color = (0, 0, 255)  # Red
    elif accuracy < 70:
        bar_color = (0, 165, 255)  # Orange
    else:
        bar_color = (0, 255, 0)  # Green

    cv.rectangle(result, (bar_x, bar_y), (bar_x + progress_width, bar_y + bar_height),
                bar_color, -1)

    # Save if path provided
    if save_path:
        ensure_temp_dir()
        cv.imwrite(save_path, result)

    return result


def visualize_results(frame, neck_region=None, fretboard=None, hand_landmarks=None,
                     finger_positions=None, chord_name=None, target_chord=None,
                     accuracy=None, save_path=None):
    """
    Create a comprehensive visualization of detection and analysis results.

    Args:
        frame: Input video frame
        neck_region: Detected neck region
        fretboard: Virtual fretboard model
        hand_landmarks: MediaPipe hand landmarks
        finger_positions: Mapped finger positions
        chord_name: Detected chord name
        target_chord: Target chord (for learning feedback)
        accuracy: Accuracy score (for learning feedback)
        save_path: Optional path to save the visualization

    Returns:
        Comprehensive visualization
    """
    result = frame.copy()

    # If neck region is detected, visualize it
    if neck_region is not None:
        result = visualize_guitar_detection(result, neck_box=neck_region)

        # Extract neck region for further visualization
        x, y, w, h = neck_region
        neck_image = frame[y:y+h, x:x+w]

        # Create fretboard visualization
        if fretboard is not None:
            fretboard_viz = visualize_fretboard(neck_image, fretboard,
                                              finger_positions, chord_name)

            # Overlay fretboard visualization back to the original image
            result[y:y+h, x:x+w] = fretboard_viz

    # Draw hand landmarks if available
    if hand_landmarks is not None:
        mp_drawing = mp.solutions.drawing_utils
        mp_hands = mp.solutions.hands
        mp_drawing_styles = mp.solutions.drawing_styles

        for landmarks in hand_landmarks:
            mp_drawing.draw_landmarks(
                result,
                landmarks,
                mp_hands.HAND_CONNECTIONS,
                mp_drawing_styles.get_default_hand_landmarks_style(),
                mp_drawing_styles.get_default_hand_connections_style()
            )

    # Add chord feedback if target and detected chords are provided
    if target_chord is not None and chord_name is not None and accuracy is not None:
        result = create_chord_feedback(result, target_chord, chord_name, accuracy)
    elif chord_name is not None:
        # Just add chord name as status
        result = create_status_bar(result, f"Chord: {chord_name}")

    # Save if path provided
    if save_path:
        ensure_temp_dir()
        cv.imwrite(save_path, result)

    return result
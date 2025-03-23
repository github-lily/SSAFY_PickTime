"""
Chord recognition module for guitar chord assistant.

This module provides functions to recognize guitar chords from finger positions
mapped to a fretboard model.
"""

import os
import json
import numpy as np
from typing import Dict, List, Tuple, Optional, Any

# Dictionary of common guitar chords in standard tuning (E A D G B E)
# Format: {chord_name: [fret_positions]}
# where fret_positions is a list of 6 integers representing fret positions on each string
# -1 means string not played, 0 means open string
COMMON_CHORDS = {
    # Major chords
    "C": [0, 3, 2, 0, 1, 0],
    "D": [-1, -1, 0, 2, 3, 2],
    "E": [0, 2, 2, 1, 0, 0],
    "F": [1, 3, 3, 2, 1, 1],
    "G": [3, 2, 0, 0, 0, 3],
    "A": [0, 0, 2, 2, 2, 0],
    "B": [-1, 2, 4, 4, 4, 2],

    # Minor chords
    "Cm": [3, 3, 5, 5, 4, 3],
    "Dm": [-1, -1, 0, 2, 3, 1],
    "Em": [0, 2, 2, 0, 0, 0],
    "Fm": [1, 3, 3, 1, 1, 1],
    "Gm": [3, 5, 5, 3, 3, 3],
    "Am": [0, 0, 2, 2, 1, 0],
    "Bm": [-1, 2, 4, 4, 3, 2],

    # 7th chords
    "C7": [0, 3, 2, 3, 1, 0],
    "D7": [-1, -1, 0, 2, 1, 2],
    "E7": [0, 2, 0, 1, 0, 0],
    "F7": [1, 3, 1, 2, 1, 1],
    "G7": [3, 2, 0, 0, 0, 1],
    "A7": [0, 0, 2, 0, 2, 0],
    "B7": [-1, 2, 1, 2, 0, 2],

    # Major 7th chords
    "Cmaj7": [0, 3, 2, 0, 0, 0],
    "Dmaj7": [-1, -1, 0, 2, 2, 2],
    "Emaj7": [0, 2, 1, 1, 0, 0],
    "Fmaj7": [1, 3, 2, 2, 1, 1],
    "Gmaj7": [3, 2, 0, 0, 0, 2],
    "Amaj7": [0, 0, 2, 1, 2, 0],
    "Bmaj7": [-1, 2, 4, 3, 4, 2],

    # Minor 7th chords
    "Cm7": [3, 3, 5, 3, 4, 3],
    "Dm7": [-1, -1, 0, 2, 1, 1],
    "Em7": [0, 2, 0, 0, 0, 0],
    "Fm7": [1, 3, 1, 1, 1, 1],
    "Gm7": [3, 5, 3, 3, 3, 3],
    "Am7": [0, 0, 2, 0, 1, 0],
    "Bm7": [-1, 2, 0, 2, 0, 2],

    # Sus4 chords
    "Csus4": [0, 3, 3, 0, 1, 0],
    "Dsus4": [-1, -1, 0, 2, 3, 3],
    "Esus4": [0, 2, 2, 2, 0, 0],
    "Fsus4": [1, 3, 3, 3, 1, 1],
    "Gsus4": [3, 3, 0, 0, 1, 3],
    "Asus4": [0, 0, 2, 2, 3, 0],
    "Bsus4": [-1, 2, 4, 4, 5, 2],

    # Add9 chords
    "Cadd9": [0, 3, 2, 0, 3, 0],
    "Dadd9": [-1, 5, 4, 2, 3, 0],
    "Eadd9": [0, 2, 2, 1, 0, 2],
    "Fadd9": [1, 0, 3, 2, 1, 1],
    "Gadd9": [3, 0, 0, 0, 0, 3],
    "Aadd9": [0, 0, 2, 2, 0, 0],
    "Badd9": [-1, 2, 4, 4, 2, 2],

    # Diminished chords
    "Cdim": [-1, 3, 4, 5, 4, -1],
    "Ddim": [-1, -1, 0, 1, 3, 1],
    "Edim": [-1, -1, 2, 3, 5, 3],
    "Fdim": [-1, -1, 3, 4, 6, 4],
    "Gdim": [3, 1, 0, 1, 0, -1],
    "Adim": [-1, 0, 1, 2, 1, -1],
    "Bdim": [-1, 2, 3, 4, 3, -1]
}


def load_chord_database(file_path=None):
    """
    Load chord database from file or use the built-in common chords.

    Args:
        file_path: Optional path to JSON file with chord definitions

    Returns:
        Dictionary of chord definitions
    """
    if file_path and os.path.exists(file_path):
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading chord database: {e}")

    # Fall back to built-in common chords
    return COMMON_CHORDS


def finger_positions_to_chord_shape(finger_positions):
    """
    Convert finger positions to a chord shape representation.

    Args:
        finger_positions: Dictionary mapping fingers to string/fret positions

    Returns:
        List of 6 integers representing fret positions on each string (-1 for not played)
    """
    # Initialize chord shape with -1 (not played)
    chord_shape = [-1] * 6

    # Fill in the positions from finger mappings
    for finger, data in finger_positions.items():
        string_idx = data.get('string', -1)
        fret_idx = data.get('fret', -1)

        # Make sure indices are valid
        if 0 <= string_idx < 6 and fret_idx >= 0:
            chord_shape[string_idx] = fret_idx

    return chord_shape


def calculate_chord_similarity(detected_shape, reference_shape):
    """
    Calculate similarity between detected chord shape and reference chord shape.

    Args:
        detected_shape: List of 6 integers from detected finger positions
        reference_shape: List of 6 integers from reference chord

    Returns:
        Similarity score (0-100)
    """
    if len(detected_shape) != 6 or len(reference_shape) != 6:
        return 0.0

    # Count matches and calculate penalty for mismatches
    matches = 0
    penalties = 0

    for i in range(6):
        # If reference says don't play this string, detected can be anything
        if reference_shape[i] == -1:
            continue

        # If detected is not played but reference is, apply penalty
        if detected_shape[i] == -1 and reference_shape[i] != -1:
            penalties += 1
            continue

        # If exact match, add a match
        if detected_shape[i] == reference_shape[i]:
            matches += 1
        else:
            # Penalty based on fret distance
            fret_diff = abs(detected_shape[i] - reference_shape[i])
            if fret_diff == 1:
                penalties += 0.5  # Small penalty for adjacent fret
            else:
                penalties += 1  # Full penalty for larger differences

    # Count strings that should be played according to reference
    played_strings = sum(1 for fret in reference_shape if fret != -1)

    # Calculate similarity (100% means perfect match)
    if played_strings == 0:
        return 0.0

    similarity = (matches / played_strings) * 100

    # Apply penalty deductions
    penalty_deduction = (penalties / played_strings) * 100
    similarity = max(0, similarity - penalty_deduction)

    return similarity


def recognize_chord(finger_positions, chord_database=None, similarity_threshold=70):
    """
    Recognize guitar chord from finger positions.

    Args:
        finger_positions: Dictionary mapping fingers to string/fret positions
        chord_database: Optional dictionary of chord definitions
        similarity_threshold: Minimum similarity score to recognize a chord

    Returns:
        Dictionary with recognized chord and confidence score
    """
    # Load chord database if not provided
    if chord_database is None:
        chord_database = load_chord_database()

    # Convert finger positions to chord shape
    detected_shape = finger_positions_to_chord_shape(finger_positions)

    # Check if we have enough fingers on the fretboard
    active_strings = sum(1 for fret in detected_shape if fret != -1)
    if active_strings < 2:
        return {
            'chord_name': 'Unknown',
            'confidence': 0.0,
            'shape': detected_shape
        }

    # Compare with each chord in the database
    best_match = {
        'chord_name': 'Unknown',
        'confidence': 0.0,
        'shape': detected_shape
    }

    for chord_name, reference_shape in chord_database.items():
        similarity = calculate_chord_similarity(detected_shape, reference_shape)

        if similarity > best_match['confidence']:
            best_match = {
                'chord_name': chord_name,
                'confidence': similarity,
                'shape': detected_shape,
                'reference_shape': reference_shape
            }

    # Apply threshold
    if best_match['confidence'] < similarity_threshold:
        best_match['chord_name'] = 'Unknown'

    return best_match


def get_chord_for_learning(level='beginner', chord_database=None):
    """
    Get a chord suitable for learning based on difficulty level.

    Args:
        level: Difficulty level ('beginner', 'intermediate', 'advanced')
        chord_database: Optional dictionary of chord definitions

    Returns:
        Dictionary with chord name and shape
    """
    # Load chord database if not provided
    if chord_database is None:
        chord_database = load_chord_database()

    # Define difficulty levels for chords
    beginner_chords = ['C', 'G', 'D', 'A', 'E', 'Am', 'Em', 'Dm']
    intermediate_chords = ['F', 'B', 'Bm', 'Cm', 'Gm', 'E7', 'A7', 'D7', 'G7', 'C7']
    advanced_chords = ['Cmaj7', 'Dmaj7', 'Amaj7', 'Fmaj7', 'Dm7', 'Am7', 'Gmaj7', 'Bm7', 'Csus4', 'Dsus4']

    # Select chord list based on level
    if level == 'beginner':
        chord_list = beginner_chords
    elif level == 'intermediate':
        chord_list = intermediate_chords
    elif level == 'advanced':
        chord_list = advanced_chords
    else:
        # Default to all chords
        chord_list = list(chord_database.keys())

    # Filter to only include chords in our database
    available_chords = [chord for chord in chord_list if chord in chord_database]

    # Return a random chord
    if not available_chords:
        # Fallback to a basic chord if no matches
        return {'chord_name': 'C', 'shape': chord_database.get('C', [0, 3, 2, 0, 1, 0])}

    import random
    selected_chord = random.choice(available_chords)

    return {
        'chord_name': selected_chord,
        'shape': chord_database[selected_chord]
    }


def compare_with_target_chord(finger_positions, target_chord):
    """
    Compare detected finger positions with a target chord for learning feedback.

    Args:
        finger_positions: Dictionary mapping fingers to string/fret positions
        target_chord: Dictionary with chord name and shape

    Returns:
        Dictionary with feedback and accuracy score
    """
    # Convert finger positions to chord shape
    detected_shape = finger_positions_to_chord_shape(finger_positions)
    reference_shape = target_chord['shape']

    # Calculate similarity
    similarity = calculate_chord_similarity(detected_shape, reference_shape)

    # Generate feedback
    feedback = []

    for i in range(6):
        string_name = ['low E', 'A', 'D', 'G', 'B', 'high E'][i]

        # Skip if string shouldn't be played
        if reference_shape[i] == -1:
            if detected_shape[i] != -1:
                feedback.append(f"String {i + 1} ({string_name}) should not be played")
            continue

        # Check if string is played
        if detected_shape[i] == -1:
            feedback.append(f"Place a finger on string {i + 1} ({string_name}) at fret {reference_shape[i]}")
            continue

        # Check if correct fret
        if detected_shape[i] != reference_shape[i]:
            if detected_shape[i] < reference_shape[i]:
                feedback.append(f"Move finger on string {i + 1} ({string_name}) up to fret {reference_shape[i]}")
            else:
                feedback.append(f"Move finger on string {i + 1} ({string_name}) down to fret {reference_shape[i]}")

    return {
        'accuracy': similarity,
        'feedback': feedback,
        'detected_shape': detected_shape,
        'target_shape': reference_shape,
        'is_correct': similarity >= 90  # Consider correct if similarity is high enough
    }


def create_chord_diagram_data(chord_name, chord_shape):
    """
    Create datasets structure for rendering a chord diagram.

    Args:
        chord_name: Name of the chord
        chord_shape: List of 6 integers representing fret positions

    Returns:
        Dictionary with chord diagram datasets
    """
    # Determine base fret (for barre chords)
    non_open_frets = [f for f in chord_shape if f > 0]
    base_fret = min(non_open_frets) if non_open_frets else 1

    # For chords that start at fret 1, no offset needed
    show_base_fret = base_fret > 1

    # Adjust fret positions if showing a section of the fretboard
    display_shapes = chord_shape.copy()

    if show_base_fret:
        for i in range(len(display_shapes)):
            if display_shapes[i] > 0:
                display_shapes[i] -= (base_fret - 1)

    # Create diagram datasets
    return {
        'chord_name': chord_name,
        'shape': chord_shape,
        'display_shape': display_shapes,
        'base_fret': base_fret,
        'show_base_fret': show_base_fret
    }
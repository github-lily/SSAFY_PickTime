"""
Hand detection module for guitar chord recognition.

This module provides functions to detect and crop guitar player's hands
using a pre-trained Faster R-CNN model with MobileNetV3 backbone.
"""

import os
import cv2 as cv
import numpy as np
import torch
import torchvision

# Default temporary directory for debug images
TMP_DIR = os.path.join("temp", "")


def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


def load_hand_detection_model(model_path=None):
    """
    Load the pre-trained hand detection model.

    Args:
        model_path: Path to model weights, if None uses default path

    Returns:
        Loaded PyTorch model
    """
    if model_path is None:
        model_path = os.path.join('models', 'hand_detection_model.zip')

    # Check if the model file exists
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Hand detection model not found at {model_path}")

    # Initialize the model
    model = torchvision.models.detection.fasterrcnn_mobilenet_v3_large_fpn(num_classes=2)

    # Load the saved state
    model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
    model.eval()

    return model


def get_boxes_with_score_over_threshold(boxes, scores, threshold, verbose=False):
    """
    Filter bounding boxes based on confidence score threshold.

    Args:
        boxes: Bounding boxes from model output
        scores: Confidence scores for each box
        threshold: Minimum score threshold
        verbose: If True, print detailed information

    Returns:
        Tuple of (filtered_boxes, filtered_scores)
    """
    final_boxes = None

    # Initial filtering by threshold
    for box, score in zip(boxes, scores):
        if score > threshold:
            if final_boxes is None:
                final_boxes = torch.clone(box).reshape(1, 4)
            else:
                final_boxes = torch.cat((final_boxes, box.reshape(1, 4)))

    # Optimizing threshold to get better hand detection
    optimized_boxes = None
    optimized_threshold = threshold
    attempts = 22
    for i in range(1, attempts + 1):
        optimized_threshold = optimized_threshold - 0.025
        if optimized_threshold < 0.26:
            optimized_threshold = 0.26

        for box, score in zip(boxes, scores):
            if score > optimized_threshold:
                if optimized_boxes is None:
                    optimized_boxes = torch.clone(box).reshape(1, 4)
                else:
                    if box not in optimized_boxes:
                        optimized_boxes = torch.cat((optimized_boxes, box.reshape(1, 4)))

        if optimized_boxes is not None:
            if ((optimized_boxes.shape[0] >= 3 and boxes.shape[0] >= 3) or
                    (optimized_boxes.shape[0] == 2 and boxes.shape[0] == 2) or
                    (i == attempts)):
                final_boxes = optimized_boxes
                threshold = optimized_threshold
                if verbose:
                    print(
                        f"{final_boxes.shape[0]} hands found with optimized threshold {threshold} after {i} attempts!")
                break

    if final_boxes is not None:
        return final_boxes, scores[:final_boxes.shape[0]]
    else:
        return None, None


def get_rightmost_box(boxes, scores, box_tolerance=550, score_tolerance=0.26, top_score=0.975, verbose=False):
    """
    Get the rightmost bounding box from detection results.
    For guitar playing, this is likely to be the left hand on the fretboard.

    Args:
        boxes: Bounding boxes tensor
        scores: Confidence scores tensor
        box_tolerance: Horizontal distance tolerance for box comparison
        score_tolerance: Score difference tolerance for box selection
        top_score: Threshold for high-confidence detections
        verbose: If True, print detailed information

    Returns:
        Selected bounding box
    """
    # Get x-coordinates of right edge of each box
    values = boxes[:, 2]

    # Initialize with the rightmost box
    rightmost_box_idx = torch.argmax(values)
    rightmost_box_score = scores[rightmost_box_idx].item()
    rightmost_box_offset = values[rightmost_box_idx].item()

    # Get box with highest score
    max_score_idx = torch.argmax(scores)
    max_score = scores[max_score_idx].item()
    max_offset = values[max_score_idx].item()

    # If the highest scoring box is significantly better than the rightmost box
    # and is not too far from it, use the highest scoring box
    if scores[max_score_idx] - scores[rightmost_box_idx] >= score_tolerance:
        if abs(max_offset - rightmost_box_offset) <= box_tolerance:
            rightmost_box_idx = max_score_idx
            rightmost_box_score = scores[rightmost_box_idx].item()
            rightmost_box_offset = values[rightmost_box_idx].item()
            if verbose:
                print(
                    f"Using highest scoring box instead of rightmost (score diff: {scores[max_score_idx] - scores[rightmost_box_idx]:.2f})")

    # Find boxes with similar scores
    a = rightmost_box_score - 0.25
    b = rightmost_box_score + 0.25
    mask = (scores <= b) & (scores >= a)

    # If multiple boxes have similar scores, choose the rightmost
    if boxes[mask].shape[0] >= 2:
        values = torch.where(mask, boxes[:, 2], torch.full_like(boxes[:, 2], -1000))
        rightmost_box_idx = torch.argmax(values)
        rightmost_box_score = scores[rightmost_box_idx].item()
        rightmost_box_offset = values[rightmost_box_idx].item()
        if verbose:
            print(f"Selected rightmost box from multiple candidates with scores in range [{a:.2f}, {b:.2f}]")

    # Final refinement: check for high-confidence boxes
    top_mask = (scores >= top_score) & (scores != rightmost_box_score)
    if boxes[top_mask].shape[0] >= 1:
        top_values = torch.where(top_mask, boxes[:, 2], torch.full_like(boxes[:, 2], -1000))
        rightmost_top_box_idx = torch.argmax(top_values)
        rightmost_top_box_score = scores[rightmost_top_box_idx].item()
        rightmost_top_box_offset = top_values[rightmost_top_box_idx].item()

        if (abs(rightmost_top_box_offset - rightmost_box_offset) <= box_tolerance or
                abs(rightmost_top_box_score - rightmost_box_score) > 0.52):
            rightmost_box_idx = rightmost_top_box_idx
            rightmost_box_score = rightmost_top_box_score
            rightmost_box_offset = values[rightmost_box_idx].item()
            if verbose:
                print(f"Selected high-confidence box (score: {rightmost_box_score:.2f})")

    return boxes[rightmost_box_idx, :]


def perform_cropping(image, box, padding):
    """
    Crop image around a bounding box with added padding.

    Args:
        image: Input image tensor (c, h, w)
        box: Bounding box coordinates [x1, y1, x2, y2]
        padding: Amount of padding to add around the box

    Returns:
        Cropped image tensor
    """
    c, h, w = image.shape

    # Calculate crop coordinates with padding
    x1 = box[0] if box[0] - padding < 0 else box[0] - padding
    y1 = box[1] if box[1] - padding < 0 else box[1] - padding
    x2 = box[2] if box[2] + padding > w else box[2] + padding
    y2 = box[3] if box[3] + padding > h else box[3] + padding

    # Convert to integers
    x1 = int(torch.floor(x1))
    y1 = int(torch.floor(y1))
    x2 = int(torch.floor(x2))
    y2 = int(torch.floor(y2))

    return image[:, y1:y2, x1:x2]


def detect_hand_in_image(img, threshold=0.79, model=None, model_path=None, save_img=False, verbose=False):
    """
    Detect and crop hand in an image.

    Args:
        img: Input image as BGR PyTorch tensor (h, w, c)
        threshold: Detection confidence threshold
        model: Pre-loaded detection model (if None, will load)
        model_path: Path to model weights (if None, uses default)
        save_img: If True, save debug images
        verbose: If True, print detailed information

    Returns:
        Cropped image around the detected hand, or original if no hand detected
    """
    # Create temp directory if needed
    if save_img:
        ensure_temp_dir()

    # Load model if not provided
    if model is None:
        try:
            model = load_hand_detection_model(model_path)
        except FileNotFoundError as e:
            print(f"Warning: {e}")
            print("Using original image without hand detection.")
            return img

    # Convert BGR PyTorch tensor to RGB and normalize for model input
    img_rgb = cv.cvtColor(img.numpy(), cv.COLOR_BGR2RGB)
    img_rgb_tensor = torch.from_numpy(img_rgb)
    model_input = img_rgb_tensor.permute(2, 0, 1).float() / 255.0

    # Perform detection
    with torch.no_grad():
        model_output = model(model_input.unsqueeze(0))

    boxes = model_output[0]['boxes']
    scores = model_output[0]['scores']

    # Filter boxes by threshold
    boxes, scores = get_boxes_with_score_over_threshold(boxes, scores, threshold, verbose)

    # If no hands detected, return original image
    if boxes is None or len(boxes) == 0:
        if verbose:
            print(f"No hands detected in the image with threshold {threshold}")
        return img

    # Get the rightmost box (likely the left hand on fretboard)
    box = get_rightmost_box(boxes, scores, verbose=verbose)

    # Save debug image if requested
    if save_img:
        debug_img = img_rgb_tensor.numpy().copy()
        for b in boxes:
            b_np = b.detach().numpy().astype(int)
            cv.rectangle(debug_img, (b_np[0], b_np[1]), (b_np[2], b_np[3]), (0, 0, 255), 2)

        # Highlight the selected box
        box_np = box.detach().numpy().astype(int)
        cv.rectangle(debug_img, (box_np[0], box_np[1]), (box_np[2], box_np[3]), (0, 255, 0), 3)

        # Save the debug image
        cv.imwrite(os.path.join(TMP_DIR, 'hand_detection.jpg'), cv.cvtColor(debug_img, cv.COLOR_RGB2BGR))

    # Crop the image around the detected hand
    image_chw = img.permute(2, 0, 1)
    cropped_image = perform_cropping(image_chw, box, padding=100)

    # Convert back to HWC format
    result = cropped_image.permute(1, 2, 0)

    # Save cropped image if requested
    if save_img:
        cv.imwrite(os.path.join(TMP_DIR, 'cropped_hand.jpg'), result.numpy())

    return result


def draw_hand_detections(image, boxes, selected_box=None):
    """
    Draw bounding boxes on an image for visualization.

    Args:
        image: Input image (numpy array)
        boxes: List of bounding boxes to draw
        selected_box: Highlighted box (if any)

    Returns:
        Image with drawn boxes
    """
    result = image.copy()

    # Draw all boxes in red
    for box in boxes:
        if isinstance(box, torch.Tensor):
            box = box.detach().cpu().numpy()
        box = box.astype(int)
        cv.rectangle(result, (box[0], box[1]), (box[2], box[3]), (0, 0, 255), 2)

    # Draw selected box in green
    if selected_box is not None:
        if isinstance(selected_box, torch.Tensor):
            selected_box = selected_box.detach().cpu().numpy()
        selected_box = selected_box.astype(int)
        cv.rectangle(result,
                     (selected_box[0], selected_box[1]),
                     (selected_box[2], selected_box[3]),
                     (0, 255, 0), 3)

    return result
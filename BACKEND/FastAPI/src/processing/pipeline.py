"""
Image processing pipeline for guitar chord recognition.

This module combines hand detection, geometric transformations, and image processing
techniques to prepare guitar images for chord classification.
"""

import os
import cv2 as cv
import numpy as np
import torch
import matplotlib.pyplot as plt

# Import from our refactored modules
from src.processing.image_processing import (
    blending, negative, increase_brightness, decrease_brightness,
    saturation, contrast_stretching, sharpening, frei_and_chen_edges
)
from src.processing.geometric_transform import correct_angle
from src.detection.hand_detection import detect_hand_in_image

# Default temporary directory
TMP_DIR = os.path.join("temp", "")

# Ensure temp directory exists
def ensure_temp_dir():
    """Ensure temporary directory exists."""
    if not os.path.exists(TMP_DIR):
        print(f"Creating temporary directory at {TMP_DIR}...")
        os.makedirs(TMP_DIR, exist_ok=True)


def process_image(img=None, filename=None, crop=True, process=True, process_mode=0, rotate=True,
                  rotate_first=True, verbose=False, save_images=False):
    """
    Process an image for chord classification by applying hand detection,
    angle correction, and image enhancement.

    Args:
        img: Input image as BGR PyTorch tensor (shape: h, w, c)
        filename: Path to image file (alternative to providing img)
        crop: If True, crop image around the hand playing the chord
        process: If True, apply image processing operations
        process_mode: Processing mode to use (0-6)
        rotate: If True, apply angle correction
        rotate_first: If True, rotate before processing (recommended for some modes)
        verbose: If True, print detailed information
        save_images: If True, save intermediate results

    Returns:
        Processed image as BGR PyTorch tensor
    """
    # Create temp directory if saving images
    if save_images:
        ensure_temp_dir()

    # Load image from file if filename is provided
    if filename:
        img = cv.imread(filename)
        if img is None:
            raise ValueError(f"Could not read image from {filename}")
        img = torch.from_numpy(img)

    # Initialize output with input image
    out = img

    # Step 1: Hand detection and cropping
    if crop:
        out = detect_hand_in_image(out, threshold=0.79, padding=100, verbose=verbose, save_img=save_images)

    # Step 2: Angle correction (before processing if rotate_first is True)
    if rotate and rotate_first:
        out = correct_angle(out, threshold=270, verbose=verbose, save_images=save_images)

    # Step 3: Image processing
    if process:
        # Intervals for contrast stretching
        bgr_mins = [0, 0, 0]
        bgr_maxs = [180, 180, 180]
        bgr_mins_1 = [0, 0, 0]
        bgr_maxs_1 = [255, 255, 255]

        # Apply different processing modes
        if process_mode == 0:
            # Frei & Chen edges + blending + saturation increase
            edges = frei_and_chen_edges(out)
            out = blending(out, edges, a=0.5)
            out = saturation(increase_brightness(out))

        elif process_mode == 1:
            # Canny edges + blending + saturation increase
            out = out.type(torch.uint8)
            c_edges = torch.from_numpy(cv.Canny(out.numpy(), threshold1=100, threshold2=200))
            out = blending(out, c_edges, a=0.5)
            out = saturation(increase_brightness(out))

        elif process_mode == 2:
            # Sharpening + contrast stretching
            out = sharpening(out)
            out = contrast_stretching(out, bgr_mins, bgr_maxs, bgr_mins_1, bgr_maxs_1)

        elif process_mode == 3:
            # Sharpening + contrast stretching + edge detection
            out = sharpening(out)
            out = contrast_stretching(out, bgr_mins, bgr_maxs, bgr_mins_1, bgr_maxs_1)
            out = frei_and_chen_edges(out)

        elif process_mode == 4:
            # Canny edge detection
            out = out.type(torch.uint8)
            out = torch.from_numpy(cv.Canny(out.numpy(), threshold1=100, threshold2=200))

        elif process_mode == 5:
            # Negative Canny edges + blending + saturation decrease (dark style)
            out = out.type(torch.uint8)
            c_edges = torch.from_numpy(cv.Canny(out.numpy(), threshold1=100, threshold2=200))
            out = blending(out, negative(c_edges), a=0.75)
            out = saturation(decrease_brightness(out))

        elif process_mode == 6:
            # Sharpening + contrast stretching + edge detection + blending (bright style)
            sharpen_img = sharpening(out)
            sharpen_stretched = contrast_stretching(sharpen_img, bgr_mins, bgr_maxs, bgr_mins_1, bgr_maxs_1)
            edges = frei_and_chen_edges(sharpen_stretched)
            out = blending(out, edges, a=0.5)
            out = saturation(increase_brightness(out))

    # Step 4: Angle correction (after processing if rotate_first is False)
    if rotate and not rotate_first:
        out = correct_angle(out, threshold=270, verbose=verbose, save_images=save_images)

    # Save final processed image if requested
    if save_images:
        cv.imwrite(os.path.join(TMP_DIR, 'processed.jpg'), out.numpy())

    return out


def generate_processing_comparison(image_path, output_path=None):
    """
    Generate a visual comparison of different processing techniques.

    Args:
        image_path: Path to the input image
        output_path: Path to save the comparison image (or None to use default)

    Returns:
        Matplotlib figure with processing comparisons
    """
    ensure_temp_dir()

    # Load image
    img = cv.imread(image_path)
    if img is None:
        raise ValueError(f"Could not read image from {image_path}")
    img = torch.from_numpy(img)

    # Create figure
    f, ax = plt.subplots(6, 2, figsize=(12, 20))
    f.tight_layout()
    f.subplots_adjust(hspace=0.25)

    # Intervals for contrast stretching
    bgr_mins = [0, 0, 0]
    bgr_maxs = [180, 180, 180]
    bgr_mins_1 = [0, 0, 0]
    bgr_maxs_1 = [255, 255, 255]

    # 0 - Original image
    ax[0][0].imshow(cv.cvtColor(img.numpy(), cv.COLOR_BGR2RGB))
    ax[0][0].set_title('0. Original')

    # 1 - Contrast stretching
    out_1 = contrast_stretching(img, bgr_mins, bgr_maxs, bgr_mins_1, bgr_maxs_1)
    ax[0][1].imshow(cv.cvtColor(out_1.numpy(), cv.COLOR_BGR2RGB))
    ax[0][1].set_title('1. Contrast Stretching')

    # 2 - Sharpening
    out_2 = sharpening(img)
    ax[1][0].imshow(cv.cvtColor(out_2.numpy(), cv.COLOR_BGR2RGB))
    ax[1][0].set_title('2. Sharpening')

    # 3 - Sharpening + contrast stretching
    out_3 = contrast_stretching(out_2, bgr_mins, bgr_maxs, bgr_mins_1, bgr_maxs_1)
    ax[1][1].imshow(cv.cvtColor(out_3.numpy(), cv.COLOR_BGR2RGB))
    ax[1][1].set_title('3. Sharpening + Contrast Stretching')

    # 4 - Frei & Chen edge detection
    out_4 = frei_and_chen_edges(img)
    ax[2][0].imshow(cv.cvtColor(out_4.numpy(), cv.COLOR_BGR2RGB))
    ax[2][0].set_title('4. Frei & Chen edges')

    # 5 - Sharpening + contrast stretching + Frei & Chen edges
    out_5 = frei_and_chen_edges(out_3)
    ax[2][1].imshow(cv.cvtColor(out_5.numpy(), cv.COLOR_BGR2RGB))
    ax[2][1].set_title('5. Sharpening + Contrast Stretching + Frei & Chen edges')

    # 6 - Canny edge detection
    out_6 = cv.Canny(img.numpy(), threshold1=100, threshold2=200)
    out_6 = torch.from_numpy(out_6)
    ax[3][0].imshow(out_6.numpy(), cmap='gray')
    ax[3][0].set_title('6. Canny edges')

    # 7 - Negative Canny edges
    out_7 = negative(out_6)
    ax[3][1].imshow(cv.cvtColor(out_7.numpy(), cv.COLOR_BGR2RGB))
    ax[3][1].set_title('7. Negative Canny edges')

    # 8 - Saturated dark image blending
    out_8 = blending(img, negative(out_6), a=0.75)
    out_8 = saturation(decrease_brightness(out_8))
    ax[4][0].imshow(cv.cvtColor(out_8.numpy(), cv.COLOR_BGR2RGB))
    ax[4][0].set_title('8. Saturated Dark Blending: Original + 7 (α=0.75)')

    # 9 - Saturated light image blending with out 5
    out_9 = blending(img, out_5, a=0.5)
    out_9 = saturation(increase_brightness(out_9))
    ax[4][1].imshow(cv.cvtColor(out_9.numpy(), cv.COLOR_BGR2RGB))
    ax[4][1].set_title('9. Saturated Bright Blending: Original + 5 (α=0.5)')

    # 10 - Saturated light image blending with Frei & Chen edges
    fc_edges = frei_and_chen_edges(img)
    out_10 = blending(img, fc_edges, a=0.5)
    out_10 = saturation(increase_brightness(out_10))
    ax[5][0].imshow(cv.cvtColor(out_10.numpy(), cv.COLOR_BGR2RGB))
    ax[5][0].set_title('10. Saturated Bright Blending: Original + Frei & Chen edges (α=0.5)')

    # 11 - Saturated light image blending with Canny edges
    c_edges = torch.from_numpy(cv.Canny(img.numpy(), threshold1=100, threshold2=200))
    out_11 = blending(img, c_edges, a=0.5)
    out_11 = saturation(increase_brightness(out_11))
    ax[5][1].imshow(cv.cvtColor(out_11.numpy(), cv.COLOR_BGR2RGB))
    ax[5][1].set_title('11. Saturated Bright Blending: Original + Canny edges (α=0.5)')

    # Save comparison image
    if output_path is None:
        output_path = os.path.join(TMP_DIR, 'image_processing_comparison.jpg')
    plt.savefig(output_path)

    return f
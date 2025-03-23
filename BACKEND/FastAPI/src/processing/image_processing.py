"""
Classical image processing operators and edge detectors for guitar chord recognition.

This module provides various image processing functions optimized for guitar chord
recognition, including edge detection, contrast enhancement, and blending operations.
"""

import cv2 as cv
import numpy as np
import torch


def blending(img1, img2, a):
    """
    Blend two images with a specified alpha value.

    Args:
        img1: First image (BGR PyTorch tensor of shape [h, w, c])
        img2: Second image (PyTorch tensor)
        a: Alpha value for blending in range [0, 1]

    Returns:
        Blended image as PyTorch tensor
    """
    if a < 0 or a > 1:
        print("Warning: The parameter 'a' must be in [0, 1] interval. Skipping blending ...")
        return img1

    img1 = img1.type(torch.float32)
    img2 = img2.type(torch.float32)

    # Ensure images have 3 dimensions
    if len(img1.shape) != 3:
        img1 = img1.unsqueeze(2)
    if len(img2.shape) != 3:
        img2 = img2.unsqueeze(2)

    out = a * img1 + (1 - a) * img2
    return out.type(torch.uint8)


def negative(img):
    """
    Apply negative effect to an image.

    Args:
        img: Input image (BGR PyTorch tensor)

    Returns:
        Negative image as PyTorch tensor
    """
    out = -1 * img + 255
    return out


def adjust_brightness(img, factor=1.0, offset=0):
    """
    Adjust the brightness of an image.

    Args:
        img: Input image (BGR PyTorch tensor)
        factor: Multiplication factor (>1 increases brightness, <1 decreases)
        offset: Value to add after multiplication

    Returns:
        Brightness adjusted image as PyTorch tensor
    """
    img = img.type(torch.float32)
    out = img * factor + offset
    out = torch.round(out).clip(0, 255)
    return out.type(torch.uint8)


def increase_brightness(img):
    """
    Increase the brightness of an image.

    Args:
        img: Input image (BGR PyTorch tensor)

    Returns:
        Brightness increased image as PyTorch tensor
    """
    return adjust_brightness(img, factor=1.8, offset=10)


def decrease_brightness(img):
    """
    Decrease the brightness of an image.

    Args:
        img: Input image (BGR PyTorch tensor)

    Returns:
        Brightness decreased image as PyTorch tensor
    """
    return adjust_brightness(img, factor=0.8, offset=-10)


def adjust_saturation(img, factor=1.0):
    """
    Adjust the saturation of an image in HSV color space.

    Args:
        img: Input image (BGR PyTorch tensor)
        factor: Multiplication factor for saturation (>1 increases, <1 decreases)

    Returns:
        Saturation adjusted image as PyTorch tensor
    """
    img = img.type(torch.float32)
    # Convert to HSV space
    hsv = cv.cvtColor(img.numpy(), cv.COLOR_BGR2HSV)
    # Adjust saturation
    hsv[:, :, 1] = hsv[:, :, 1] * factor
    # Convert back to BGR
    out = cv.cvtColor(hsv, cv.COLOR_HSV2BGR)
    out = torch.from_numpy(out)
    out = torch.round(out).clip(0, 255)
    return out.type(torch.uint8)


def saturation(img):
    """
    Increase the saturation of an image.

    Args:
        img: Input image (BGR PyTorch tensor)

    Returns:
        Saturation increased image as PyTorch tensor
    """
    return adjust_saturation(img, factor=1.5)


def desaturation(img):
    """
    Decrease the saturation of an image.

    Args:
        img: Input image (BGR PyTorch tensor)

    Returns:
        Saturation decreased image as PyTorch tensor
    """
    return adjust_saturation(img, factor=0.5)


def contrast_stretching(img, bgr_mins, bgr_maxs, bgr_mins_1, bgr_maxs_1):
    """
    Apply contrast stretching to an image.

    Args:
        img: Input image (BGR PyTorch tensor)
        bgr_mins: List of minimum values for each channel [B, G, R]
        bgr_maxs: List of maximum values for each channel [B, G, R]
        bgr_mins_1: List of target minimum values for each channel [B, G, R]
        bgr_maxs_1: List of target maximum values for each channel [B, G, R]

    Returns:
        Contrast stretched image as PyTorch tensor
    """
    img = img.type(torch.float32)
    if len(img.shape) != 3:
        img = img.unsqueeze(2)
    out = torch.clone(img)

    for c in range(img.shape[2]):
        out[:, :, c][out[:, :, c] <= bgr_mins[c]] = bgr_mins_1[c]
        out[:, :, c][out[:, :, c] >= bgr_maxs[c]] = bgr_maxs_1[c]

        mask = (out[:, :, c] > bgr_mins[c]) & (out[:, :, c] < bgr_maxs[c])
        scale_factor = (bgr_maxs_1[c] - bgr_mins_1[c]) / (bgr_maxs[c] - bgr_mins[c])

        out[:, :, c][mask] = (out[:, :, c][mask] - bgr_mins[c]) * scale_factor + bgr_mins_1[c]

    return out.type(torch.uint8)


def sharpening(img):
    """
    Apply sharpening filter to an image.

    Args:
        img: Input image (BGR PyTorch tensor)

    Returns:
        Sharpened image as PyTorch tensor
    """
    img = img.numpy().astype(np.float32)
    # A >= 1; W = 9A - 1
    A = 1.1
    W = 9 * A - 1
    kernel = np.array([[-1, -1, -1],
                       [-1, W, -1],
                       [-1, -1, -1]])
    out = cv.filter2D(img, -1, kernel)
    out = np.around(out)
    out = np.clip(out, 0, 255)

    out = torch.from_numpy(out).type(torch.uint8)
    return out


def get_gradient(im, kernel_x, kernel_y):
    """
    Calculate gradient magnitude and direction using provided kernels.

    Args:
        im: Input image (numpy array)
        kernel_x: X-direction gradient kernel
        kernel_y: Y-direction gradient kernel

    Returns:
        Tuple of (magnitude, direction) arrays
    """
    # Applying kernel gradient masks
    Gx = cv.filter2D(im, -1, kernel_x)
    Gy = cv.filter2D(im, -1, kernel_y)
    # Getting magnitude and direction
    M = np.sqrt(Gx ** 2 + Gy ** 2)
    D = np.arctan2(Gy, Gx)
    return M, D


def get_grayscale_image_from_gradient(M, max_magnitude_value, use_maximum_array_value=False, color_image=True):
    """
    Convert gradient magnitude to a grayscale image.

    Args:
        M: Magnitude array
        max_magnitude_value: Maximum possible magnitude value
        use_maximum_array_value: If True, normalize by the maximum value in the array
        color_image: If True, process each channel separately

    Returns:
        Grayscale image as numpy uint8 array
    """
    # Even if we could divide by the maximum possible value, it's better to divide by the actual maximum.
    # In this way, the contours of image are more evident.
    if use_maximum_array_value:
        if color_image:
            for i in range(3):
                M[:, :, i] = M[:, :, i] / np.max(M[:, :, i]) * 255
        else:
            M = M / np.max(M) * 255
    else:
        M = M / max_magnitude_value * 255
    M = np.clip(np.around(M), 0, 255).astype(np.uint8)

    return M


def frei_and_chen_edges(img, threshold1=40, threshold2=255):
    """
    Detect edges using Frei and Chen operator.

    Args:
        img: Input image (BGR PyTorch tensor)
        threshold1: Lower threshold for edge detection
        threshold2: Upper threshold for edge detection

    Returns:
        Edge image as PyTorch tensor
    """
    img = img.numpy()
    img_gray = cv.cvtColor(img, cv.COLOR_BGR2GRAY)

    frei_and_chen_x = np.array([[-1, 0, 1], [-np.sqrt(2), 0, np.sqrt(2)], [-1, 0, 1]], dtype=np.float32)
    frei_and_chen_y = np.array([[-1, -np.sqrt(2), -1], [0, 0, 0], [1, np.sqrt(2), 1]], dtype=np.float32)
    frei_and_chen_max_magnitude_value = 1232

    M, D = get_gradient(img_gray.astype(np.float32), frei_and_chen_x, frei_and_chen_y)
    out = get_grayscale_image_from_gradient(M, frei_and_chen_max_magnitude_value,
                                           use_maximum_array_value=True, color_image=False)

    # Final edge thresholding
    out[threshold1 <= out.all() <= threshold2] = 1
    out[out < threshold1] = 0
    out[out > threshold2] = 0

    out = torch.from_numpy(out).type(torch.uint8)
    return out


def canny_edges(img, threshold1=100, threshold2=200):
    """
    Detect edges using Canny operator.

    Args:
        img: Input image (BGR PyTorch tensor)
        threshold1: Lower threshold for edge detection
        threshold2: Upper threshold for edge detection

    Returns:
        Edge image as PyTorch tensor
    """
    img = img.numpy()
    edges = cv.Canny(img, threshold1, threshold2)
    return torch.from_numpy(edges).type(torch.uint8)
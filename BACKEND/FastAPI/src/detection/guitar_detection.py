"""
Guitar neck detection module using YOLOv12.

This module provides functions to detect guitar necks in images
using a trained YOLOv12 model.
"""

import os
import cv2 as cv
import numpy as np
import torch
from ultralytics import YOLO
import time


class GuitarNeckDetector:
    """
    Guitar neck detector using YOLOv12.

    This class loads a trained YOLOv12 model and provides methods
    to detect guitar necks in images and video frames.
    """

    def __init__(self, model_path="models/guitar_neck_detection/train/weights/best.pt",
                 conf_threshold=0.5, device=None):
        """
        Initialize the guitar neck detector.

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

        print(f"Loading YOLOv12 model from {model_path} on device {self.device}")

        # Load model
        try:
            self.model = YOLO(model_path)
            print("Model loaded successfully")
        except Exception as e:
            print(f"Error loading model: {str(e)}")
            raise e

        self.conf_threshold = conf_threshold
        self.class_names = ['neck', 'nut']  # Based on training datasets

    def detect(self, image, return_crops=True):
        """
        Detect guitar necks in an image.

        Args:
            image: Image to process (numpy array or path)
            return_crops: If True, return cropped neck images

        Returns:
            Dictionary with detection results and neck crops
        """
        # Run detection
        results = self.model(image, conf=self.conf_threshold, device=self.device)

        # Process results
        detections = []
        crops = []

        for r in results:
            boxes = r.boxes
            if len(boxes) == 0:
                continue

            # Get the original image
            orig_img = r.orig_img

            # Process each detection
            for box in boxes:
                # Get class and confidence
                cls = int(box.cls[0].item())
                conf = float(box.conf[0].item())

                # Get bounding box
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy().astype(int)

                # Add to detections list
                detection = {
                    'class': cls,
                    'class_name': self.class_names[cls],
                    'confidence': conf,
                    'bbox': [x1, y1, x2, y2]
                }
                detections.append(detection)

                # Crop the neck region if requested
                if return_crops and cls == 0:  # If it's a neck
                    crop = orig_img[y1:y2, x1:x2].copy()
                    crops.append({
                        'image': crop,
                        'position': [x1, y1, x2, y2]
                    })

        result = {
            'detections': detections,
            'crops': crops if return_crops else None
        }

        return result

    def visualize_detections(self, image, detections):
        """
        Visualize guitar neck detections on an image.

        Args:
            image: Image to visualize
            detections: Detection results from detect method

        Returns:
            Image with visualizations
        """
        # Make a copy of the image
        vis_image = image.copy()

        # Draw each detection
        for det in detections['detections']:
            # Get bounding box
            x1, y1, x2, y2 = det['bbox']

            # Get class and confidence
            cls_name = det['class_name']
            conf = det['confidence']

            # Choose color based on class
            color = (0, 255, 0) if cls_name == 'neck' else (0, 0, 255)

            # Draw bounding box
            cv.rectangle(vis_image, (x1, y1), (x2, y2), color, 2)

            # Draw label
            label = f"{cls_name}: {conf:.2f}"
            cv.putText(vis_image, label, (x1, y1 - 10),
                      cv.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        return vis_image


def process_frame(frame, detector, fretboard_detector, visualize=True, save_debug=False):
    """
    Process a video frame to detect guitar necks and create virtual fretboard.

    Args:
        frame: Video frame to process
        detector: GuitarNeckDetector instance
        fretboard_detector: Module for fretboard detection
        visualize: If True, create visualization
        save_debug: If True, save debug images

    Returns:
        Dictionary with processing results and visualization
    """
    # Detect guitar necks
    detections = detector.detect(frame)

    # Initialize result
    result = {
        'success': False,
        'fretboard': None,
        'visualization': None
    }

    # Process neck crops
    if detections['crops']:
        # Take the first neck crop
        neck_crop = detections['crops'][0]['image']
        neck_position = detections['crops'][0]['position']

        # Detect strings and frets
        lines = fretboard_detector.detect_strings_and_frets(
            neck_crop,
            string_threshold=250,
            fret_threshold=100,
            verbose=True,
            save_debug=save_debug
        )

        # Create virtual fretboard
        if lines['strings'] is not None and lines['frets'] is not None:
            fretboard = fretboard_detector.create_virtual_fretboard(
                lines['strings'],
                lines['frets'],
                neck_crop.shape,
                save_debug=save_debug
            )

            if fretboard_detector.is_valid_fretboard(fretboard):
                result['success'] = True
                result['fretboard'] = fretboard
                result['neck_position'] = neck_position

                # Create visualization if requested
                if visualize:
                    # Visualize neck detection
                    vis_frame = detector.visualize_detections(frame, detections)

                    # Visualize fretboard in the neck crop
                    vis_neck = fretboard_detector.visualize_fretboard(neck_crop, fretboard)

                    # Insert the visualized neck back into the frame
                    x1, y1, x2, y2 = neck_position
                    vis_frame[y1:y2, x1:x2] = vis_neck

                    result['visualization'] = vis_frame

    # If no visualization was created but it was requested
    if visualize and result['visualization'] is None:
        result['visualization'] = detector.visualize_detections(frame, detections)

    return result


def run_webcam_detection(detector, fretboard_detector, cam_id=0, output_fps=30):
    """
    Run guitar neck detection on webcam feed.

    Args:
        detector: GuitarNeckDetector instance
        fretboard_detector: Module for fretboard detection
        cam_id: Camera ID for webcam
        output_fps: Target FPS for output video
    """
    # Open webcam
    cap = cv.VideoCapture(cam_id)
    if not cap.isOpened():
        print(f"Error: Could not open webcam {cam_id}")
        return

    print(f"Started webcam detection. Press 'q' to quit.")

    # Main processing loop
    last_valid_fretboard = None
    last_valid_time = 0

    while True:
        # Read frame
        ret, frame = cap.read()
        if not ret:
            print("Error: Could not read frame from webcam")
            break

        # Process the frame
        result = process_frame(frame, detector, fretboard_detector)

        # Update last valid fretboard if successful
        if result['success']:
            last_valid_fretboard = result['fretboard']
            last_valid_time = time.time()

        # Use the visualization from the result
        vis_frame = result['visualization']

        # Add FPS information
        cv.putText(vis_frame, f"Press 'q' to quit", (10, 30),
                  cv.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)

        # Show the frame
        cv.imshow('Guitar Neck Detection', vis_frame)

        # Check for quit key
        if cv.waitKey(1) & 0xFF == ord('q'):
            break

    # Release resources
    cap.release()
    cv.destroyAllWindows()


if __name__ == "__main__":
    import argparse
    import sys

    # Add src directory to path to import modules
    sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

    from src.detection import fretboard_detection

    parser = argparse.ArgumentParser(description='Guitar neck detection from webcam')
    parser.add_argument('--model', type=str, default='models/guitar_neck_detection/train/weights/best.pt',
                        help='Path to trained YOLOv12 model')
    parser.add_argument('--camera', type=int, default=0, help='Camera ID for webcam')
    parser.add_argument('--conf', type=float, default=0.5, help='Confidence threshold')
    args = parser.parse_args()

    # Initialize guitar neck detector
    detector = GuitarNeckDetector(model_path=args.model, conf_threshold=args.conf)

    # Run webcam detection
    run_webcam_detection(detector, fretboard_detection, cam_id=args.camera)
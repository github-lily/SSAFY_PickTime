"""
Guitar neck detection training module using YOLOv12.

This module trains a YOLOv12 model to detect guitar necks in images,
which will be used for fretboard analysis.
"""

import os
import yaml
import torch
from ultralytics import YOLO
from pathlib import Path


def create_data_yaml(dataset_dir, output_path='guitar_neck_data.yaml'):
    """
    Create the datasets.yaml configuration file for YOLOv12 training.

    Args:
        dataset_dir: Root directory of the dataset
        output_path: Path to save the YAML file
    """
    # Define class names
    class_names = ['neck', 'nut']  # Based on the provided labels

    # Construct paths
    train_images = os.path.join(os.path.abspath(dataset_dir), 'train', 'images')
    val_images = os.path.join(os.path.abspath(dataset_dir), 'valid', 'images')
    test_images = os.path.join(os.path.abspath(dataset_dir), 'test', 'images')

    # Verify paths exist
    for path in [train_images, val_images, test_images]:
        if not os.path.exists(path):
            print(f"Warning: Path does not exist: {path}")

    # Create YAML content
    data = {
        'path': os.path.abspath(dataset_dir),
        'train': train_images,
        'val': val_images,
        'test': test_images,
        'names': {0: 'neck', 1: 'nut'},
        'nc': 2  # Number of classes
    }

    # Write to file
    with open(output_path, 'w') as f:
        yaml.dump(data, f, default_flow_style=False)

    print(f"Created data configuration at {output_path} with paths:")
    print(f"  Train: {train_images}")
    print(f"  Val: {val_images}")
    print(f"  Test: {test_images}")
    return output_path


def train_yolo_model(data_yaml, model_name='models/yolov12n.pt', epochs=100, batch_size=16, image_size=640):
    """
    Train a YOLOv12 model for guitar neck detection.

    Args:
        data_yaml: Path to the datasets.yaml configuration file
        model_name: Pre-trained YOLO model to use as starting point
        epochs: Number of training epochs
        batch_size: Batch size for training
        image_size: Input image size for the model

    Returns:
        Path to the trained model
    """
    # Create output directory
    output_dir = Path('models/guitar_neck_detection')
    output_dir.mkdir(parents=True, exist_ok=True)

    # Load a pre-trained YOLOv12 model
    model = YOLO(model_name)

    # Train the model
    results = model.train(
        data=data_yaml,
        epochs=epochs,
        batch=batch_size,
        imgsz=image_size,
        patience=20,  # Early stopping patience
        device='0' if torch.cuda.is_available() else 'cpu',
        project=str(output_dir),
        name='train',
        exist_ok=True,
        pretrained=True,
        optimizer='AdamW',  # Use AdamW optimizer
        lr0=0.001,
        lrf=0.01,
        cos_lr=True,
        augment=True  # Use datasets augmentation
    )

    # Export the model to ONNX for faster inference
    model.export(format='onnx', imgsz=image_size)

    # Path to the best trained model
    best_model_path = output_dir / 'train' / 'weights' / 'best.pt'
    onnx_model_path = output_dir / 'train' / 'weights' / 'best.onnx'

    print(f"Training completed. Best model saved at: {best_model_path}")
    print(f"ONNX model for inference saved at: {onnx_model_path}")

    return str(best_model_path)


def validate_model(model_path, test_data):
    """
    Validate the trained model on test datasets.

    Args:
        model_path: Path to the trained model
        test_data: Path to test datasets directory

    Returns:
        Validation metrics
    """
    # 경로 정규화
    test_data = os.path.normpath(test_data)

    # Load the trained model
    model = YOLO(model_path)

    # Check if test directory exists
    test_images_dir = os.path.join(test_data, 'images')
    if not os.path.exists(test_images_dir):
        print(f"Warning: Test images directory not found at {test_images_dir}")
        return None

    # Run validation
    try:
        metrics = model.val(data=test_data, split='test')
        print(f"Model validation results: {metrics}")
        return metrics
    except Exception as e:
        print(f"Validation error: {str(e)}")
        return None


def main(dataset_dir, output_dir='models/guitar_neck_detection', yaml_path='../datasets/guitar_neck_dataset/data.yaml', epochs=100, batch_size=16):
    """
    Run the complete training pipeline.

    Args:
        dataset_dir: Root directory of the dataset
        output_dir: Directory to save models
        yaml_path: Path to the configuration file
        epochs: Number of training epochs
        batch_size: Batch size for training

    Returns:
        Path to the trained model
    """
    try:
        # Create datasets configuration
        data_yaml = data_yaml = create_data_yaml(dataset_dir, output_path=yaml_path)

        # Train the model
        model_path = train_yolo_model(data_yaml, epochs=epochs, batch_size=batch_size)

        # Validate the model
        test_data = os.path.join(dataset_dir, 'test')
        validate_model(model_path, test_data)

        print(f"Guitar neck detection model training complete. Model saved at {model_path}")
        return model_path

    except Exception as e:
        print(f"Training failed with error: {str(e)}")
        raise e


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Train YOLOv12 for guitar neck detection')
    parser.add_argument('--dataset_dir', type=str, required=True, help='Path to dataset root directory')
    parser.add_argument('--epochs', type=int, default=100, help='Number of training epochs')
    parser.add_argument('--batch_size', type=int, default=16, help='Training batch size')
    parser.add_argument('--image_size', type=int, default=640, help='Input image size')

    args = parser.parse_args()

    main(args.dataset_dir, epochs=args.epochs, batch_size=args.batch_size, image_size=args.image_size)
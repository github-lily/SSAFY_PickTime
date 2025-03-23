"""
Guitar strings and frets detection training module using YOLOv12.

This module trains a YOLOv12 model to detect guitar strings and frets in images,
which will be used for chord detection instead of Hough transform.
"""

import os
import yaml
import torch
from ultralytics import YOLO
from pathlib import Path
import logging

# 기존 루트 로거 가져오기
logger = logging.getLogger()

# 기본 로깅 설정 (main.py에서 이미 설정되었을 경우 영향 없음)
if not logger.handlers:
    # 만약 핸들러가 설정되지 않았을 경우에만 기본 설정
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[logging.StreamHandler()]
    )
    logger = logging.getLogger('string_fret_training')

def create_data_yaml(dataset_dir, output_path='string_fret_data.yaml'):
    """
    Create the data.yaml configuration file for YOLOv12 training.

    Args:
        dataset_dir: Root directory of the dataset
        output_path: Path to save the YAML file
    """
    # 디버깅: 입력 경로 출력
    logger.info(f"Dataset directory: {dataset_dir}")
    logger.info(f"Output path: {output_path}")

    # data.yaml 파일 경로 구성
    yaml_path = os.path.join(dataset_dir, 'data.yaml')
    logger.info(f"Looking for data.yaml at: {yaml_path}")
    logger.info(f"File exists check: {os.path.exists(yaml_path)}")

    # 파일이 존재하면 그대로 사용
    if os.path.exists(yaml_path):
        logger.info(f"Found existing data.yaml at {yaml_path}")
        # 내용 확인
        with open(yaml_path, 'r') as f:
            yaml_content = yaml.safe_load(f)
            logger.info(f"Classes: {yaml_content.get('nc')}")
            logger.info(f"Class names: {yaml_content.get('names')}")

        # 새 경로 구성 (절대 경로 사용)
        abs_dataset_dir = os.path.abspath(dataset_dir)
        yaml_content['path'] = abs_dataset_dir
        yaml_content['train'] = os.path.join(abs_dataset_dir, 'train', 'images')
        yaml_content['val'] = os.path.join(abs_dataset_dir, 'valid', 'images')
        yaml_content['test'] = os.path.join(abs_dataset_dir, 'test', 'images')

        # 경로 존재 확인
        for path_key in ['train', 'val', 'test']:
            if not os.path.exists(yaml_content[path_key]):
                logger.info(f"Warning: Path does not exist: {yaml_content[path_key]}")

        # 수정된 내용으로 새 파일 생성
        with open(output_path, 'w') as f:
            yaml.dump(yaml_content, f, default_flow_style=False)

        logger.info(f"Created modified data configuration at {output_path}")
        return output_path

    # 파일이 없으면 새로 생성
    logger.info(f"No existing data.yaml found, creating new one")

    # Define class names
    class_names = ['fret', 'nut', 'string']  # Based on the provided info

    # Construct paths
    train_images = os.path.join(dataset_dir, 'train', 'images')
    val_images = os.path.join(dataset_dir, 'valid', 'images')
    test_images = os.path.join(dataset_dir, 'test', 'images')

    # Verify paths
    for path in [train_images, val_images, test_images]:
        if not os.path.exists(path):
            logger.info(f"Warning: Path does not exist: {path}")

    # Create YAML content
    data = {
        'path': os.path.abspath(dataset_dir),
        'train': os.path.abspath(train_images),
        'val': os.path.abspath(val_images),
        'test': os.path.abspath(test_images),
        'names': {0: 'fret', 1: 'nut', 2: 'string'},  # 3개 클래스
        'nc': 3  # 클래스 수를 3으로 명시적 설정
    }

    # Write to file
    with open(output_path, 'w') as f:
        yaml.dump(data, f, default_flow_style=False)

    logger.info(f"Created data configuration at {output_path}")
    return output_path


def train_yolo_model(data_yaml, model_name='models/yolo11n-seg.pt', epochs=100, batch_size=16, image_size=640):
    """
    Train a YOLOv12 model for guitar string and fret detection.

    Args:
        data_yaml: Path to the data.yaml configuration file
        model_name: Pre-trained YOLO model to use as starting point
        epochs: Number of training epochs
        batch_size: Batch size for training
        image_size: Input image size for the model

    Returns:
        Path to the trained model
    """
    # Create output directory
    output_dir = Path('models/string_fret_detection')
    output_dir.mkdir(parents=True, exist_ok=True)

    # Load a pre-trained YOLO model (using v8 if v12 not available)
    logger.info(f"Loading segmentation model: {model_name}")
    model = YOLO(model_name)

    # Train the model
    results = model.train(
        data=data_yaml,
        epochs=epochs,
        batch=batch_size,
        imgsz=image_size,
        task='segment',
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
        augment=True,  # Use data augmentation
        # 중요: 이미지당 최대 객체 수 늘리기 (라인이 많을 수 있음)
        nbs=64,
        cache=False,  # 캐시 사용 안함 (문제 해결 시점까지)
        rect=False,  # 모자이크 증강 비활성화 (문제 해결 시점까지)
        verbose=True
    )

    # Export the model to ONNX for faster inference
    model.export(format='onnx', imgsz=image_size)

    # Path to the best trained model
    best_model_path = output_dir / 'train' / 'weights' / 'best.pt'
    onnx_model_path = output_dir / 'train' / 'weights' / 'best.onnx'

    logger.info(f"Training completed. Best model saved at: {best_model_path}")
    logger.info(f"ONNX model for inference saved at: {onnx_model_path}")

    return str(best_model_path)


def validate_model(model_path, test_data):
    """
    Validate the trained model on test data.

    Args:
        model_path: Path to the trained model
        test_data: Path to test data directory

    Returns:
        Validation metrics
    """
    # Load the trained model
    model = YOLO(model_path)

    # Run validation
    try:
        metrics = model.val(data=test_data, split='test')
        logger.info(f"Model validation results: {metrics}")
        return metrics
    except Exception as e:
        logger.info(f"Validation error: {str(e)}")
        return None


def main(dataset_dir, output_dir='models/string_fret_detection', yaml_path='data.yaml',
        epochs=100, batch_size=16, image_size=640):
    """
    Run the complete training pipeline.

    Args:
        dataset_dir: Root directory of the dataset
        output_dir: Directory to save models
        yaml_path: Path to the data.yaml configuration file
        epochs: Number of training epochs
        batch_size: Batch size for training
        image_size: Input image size for the model

    Returns:
        Path to the trained model
    """
    try:
        # Create data configuration
        data_yaml = create_data_yaml(dataset_dir, output_path=yaml_path)

        # Train the model
        model_path = train_yolo_model(
            data_yaml,
            model_name='yolov12n.pt',
            epochs=epochs,
            batch_size=batch_size,
            image_size=image_size
        )

        # Validate the model (with error handling)
        try:
            test_data = data_yaml  # Use the same yaml file that has test path
            validate_model(model_path, test_data)
        except Exception as e:
            logger.info(f"Validation warning: {str(e)}")

        logger.info(f"String and fret detection model training complete. Model saved at {model_path}")
        return model_path

    except Exception as e:
        logger.info(f"Training failed with error: {str(e)}")
        raise e


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Train YOLO for guitar string and fret detection')
    parser.add_argument('--dataset_dir', type=str, required=True, help='Path to dataset root directory')
    parser.add_argument('--epochs', type=int, default=100, help='Number of training epochs')
    parser.add_argument('--batch_size', type=int, default=16, help='Training batch size')
    parser.add_argument('--image_size', type=int, default=640, help='Input image size')

    args = parser.parse_args()

    main(args.dataset_dir, epochs=args.epochs, batch_size=args.batch_size, image_size=args.image_size)
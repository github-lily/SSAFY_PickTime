"""
Chord classification module for guitar chord recognition.

This module provides a chord classification model based on MobileNetV2,
pre-trained on ImageNet and fine-tuned for guitar chord recognition.
"""

import os
import torch
import torch.nn as nn
import torchvision
from torchvision import models

# Number of chord classes
NUM_CLASSES = 7


class SimpleCNNClassifier(nn.Module):
    """
    Simple CNN classifier for guitar chords, similar to the original ChordClassificationNetwork.
    """

    def __init__(self):
        super(SimpleCNNClassifier, self).__init__()
        self.features = nn.Sequential(
            nn.Conv2d(3, 64, (3, 3)),
            nn.ReLU(),
            nn.MaxPool2d(3),
            nn.Conv2d(64, 128, (3, 3)),
            nn.ReLU(),
            nn.MaxPool2d(3)
        )
        self.classifier = nn.Sequential(
            nn.Dropout(0.2),
            nn.Linear(56448, NUM_CLASSES),
        )

    def forward(self, x):
        x = self.features(x)
        x = torch.flatten(x, 1)
        x = self.classifier(x)
        output = torch.softmax(x, dim=1)
        return output


class MobileNetClassifier(nn.Module):
    """
    MobileNetV2-based classifier for guitar chords.
    """

    def __init__(self, pretrained=True):
        super(MobileNetClassifier, self).__init__()
        self.model = models.mobilenet_v2(pretrained=pretrained)
        # Replace the classifier
        self.model.classifier[1] = nn.Linear(self.model.classifier[1].in_features, NUM_CLASSES)

    def forward(self, x):
        return self.model(x)


def load_chord_classifier(model_type='mobilenet', model_path=None):
    """
    Load a pre-trained chord classification model.

    Args:
        model_type: Type of model to load ('simple' or 'mobilenet')
        model_path: Path to model weights, if None uses default path

    Returns:
        Loaded PyTorch model
    """
    if model_path is None:
        # Default path based on model type
        model_path = os.path.join('models', 'chord_classification',
                                  'simple_model.pth' if model_type == 'simple' else 'mobilenet_model.pth')

    # Create model based on type
    if model_type == 'simple':
        model = SimpleCNNClassifier()
    else:
        model = MobileNetClassifier(pretrained=False)

    # Check if model file exists
    if not os.path.exists(model_path):
        print(f"Warning: Chord classification model not found at {model_path}")
        print("Using untrained model...")
        return model

    try:
        # Load model weights
        model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
        model.eval()
        return model
    except Exception as e:
        print(f"Error loading model: {e}")
        print("Using untrained model...")
        return model


def classify_chord(model, image):
    """
    Classify a chord from an image using a pre-trained model.

    Args:
        model: Pre-trained chord classification model
        image: Image tensor (3, H, W) normalized to [0, 1]

    Returns:
        Tuple of (predicted_chord_index, confidence)
    """
    # Ensure image has correct dimensions
    if len(image.shape) == 3:
        image = image.unsqueeze(0)  # Add batch dimension

    # Ensure model is in eval mode
    model.eval()

    # Perform inference
    with torch.no_grad():
        outputs = model(image)
        probabilities = torch.softmax(outputs, dim=1)
        confidence, predicted = torch.max(probabilities, 1)

    return predicted.item(), confidence.item()


def chord_index_to_name(idx):
    """
    Convert chord index to chord name.

    Args:
        idx: Chord index (0-6)

    Returns:
        Chord name (A, B, C, D, E, F, G)
    """
    chord_names = ['A', 'B', 'C', 'D', 'E', 'F', 'G']
    if 0 <= idx < len(chord_names):
        return chord_names[idx]
    return "Unknown"
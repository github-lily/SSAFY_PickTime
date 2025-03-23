import tensorflow as tf
import tensorflow_hub as hub
import os
import argparse


def convert_crepe_to_tflite(model_size="tiny", output_dir="models/audio/crepe_tflite"):
    """
    CREPE 모델을 TFLite로 변환하여 저장

    Args:
        model_size: 모델 크기 ("tiny", "small", "medium", "large", "full")
        output_dir: 출력 디렉토리
    """
    # 모델 URL
    model_url = f"https://tfhub.dev/google/tfjs-model/crepe/tfjs/2/{model_size}"

    # 모델 로드
    print(f"다운로드 중: CREPE {model_size} 모델")
    crepe_model = hub.load(model_url)

    # 저장 경로
    os.makedirs(output_dir, exist_ok=True)

    # TFLite 변환
    converter = tf.lite.TFLiteConverter.from_keras_model(crepe_model)

    # 최적화
    converter.optimizations = [tf.lite.Optimize.DEFAULT]

    # 양자화
    # converter.target_spec.supported_types = [tf.float16]  # 또는 tf.int8

    # 변환
    tflite_model = converter.convert()

    # 저장
    model_path = os.path.join(output_dir, f"crepe_{model_size}.tflite")
    with open(model_path, 'wb') as f:
        f.write(tflite_model)

    print(f"모델이 저장되었습니다: {model_path}")
    print(f"모델 크기: {os.path.getsize(model_path) / (1024 * 1024):.2f} MB")

    return model_path


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="CREPE 모델을 TFLite로 변환")
    parser.add_argument("--model_size", type=str, default="tiny",
                        choices=["tiny", "small", "medium", "large", "full"],
                        help="CREPE 모델 크기")
    parser.add_argument("--output_dir", type=str, default="models/audio/crepe_tflite",
                        help="출력 디렉토리")

    args = parser.parse_args()
    convert_crepe_to_tflite(args.model_size, args.output_dir)

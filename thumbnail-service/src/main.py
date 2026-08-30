import json
import os
import time
import pika # pyright: ignore[reportMissingModuleSource]
import requests
from PIL import Image

RABBITMQ_HOST = os.environ["RABBITMQ_HOST"]
RABBITMQ_PORT = int(os.environ["RABBITMQ_PORT"])
RABBITMQ_USER = os.environ["RABBITMQ_USER"]
RABBITMQ_PASSWORD = os.environ["RABBITMQ_PASSWORD"]
BACKEND_URL = os.environ["BACKEND_URL"]
THUMBNAIL_CALLBACK_SECRET = os.environ["THUMBNAIL_CALLBACK_SECRET"]
QUEUE_NAME = "thumbnail-generation"

THUMBNAIL_SIZE = (300, 300)


def connect_to_rabbitmq(max_retries=10, delay_seconds=3):
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials,
    )

    for attempt in range(1, max_retries + 1):
        try:
            connection = pika.BlockingConnection(parameters)
            print("Connecté à RabbitMQ")
            return connection
        except pika.exceptions.AMQPConnectionError:
            print(f"RabbitMQ pas encore prêt (tentative {attempt}/{max_retries}), retry dans {delay_seconds}s")
            time.sleep(delay_seconds)

    raise RuntimeError("Impossible de se connecter à RabbitMQ après plusieurs tentatives")


def generate_thumbnail(original_path: str) -> str:
    directory = os.path.dirname(original_path)
    thumbnail_dir = os.path.join(directory, "thumbnail")
    os.makedirs(thumbnail_dir, exist_ok=True)

    filename = os.path.basename(original_path)
    thumbnail_path = os.path.join(thumbnail_dir, filename)

    with Image.open(original_path) as img:
        img.thumbnail(THUMBNAIL_SIZE)
        img.save(thumbnail_path, "WEBP")

    return thumbnail_path


def notify_backend(media_asset_id: str):
    url = f"{BACKEND_URL}/media/{media_asset_id}/thumbnail-ready"
    headers = {"X-Internal-Secret": THUMBNAIL_CALLBACK_SECRET}

    response = requests.patch(url, headers=headers, timeout=10)
    response.raise_for_status()


def on_message(channel, method, _ ,body):
    message = json.loads(body)
    media_asset_id = message["mediaAssetId"]
    storage_path = message["storagePath"]
    directory = os.path.dirname(storage_path)
    thumbnail_path = os.path.join(directory, "thumbnail", os.path.basename(storage_path))



    try:
        if os.path.exists(thumbnail_path):
            print(f"Thumbnail déjà existant pour : {media_asset_id} ({storage_path})")
            notify_backend(media_asset_id)
            print(f"Backend notifié pour {media_asset_id}")
            channel.basic_ack(delivery_tag=method.delivery_tag)
            return

        print(f"Traitement de {media_asset_id} ({storage_path})")
        thumbnail_path = generate_thumbnail(storage_path)
        print(f"Miniature créée : {thumbnail_path}")

        notify_backend(media_asset_id)
        print(f"Backend notifié pour {media_asset_id}")

        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(f"Erreur lors du traitement de {media_asset_id} : {e}")
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


def main():
    connection = connect_to_rabbitmq()
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=on_message)

    print("En attente de messages...")
    channel.start_consuming()


if __name__ == "__main__":
    main()
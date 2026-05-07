# Gestion de Procesos Desktop

## RF05 (simulacion local)

Se agrego un boton en la interfaz para simular la actividad de RF05 sin depender de la App Web. Usa los procesos capturados actuales y un TH fijo de 500 ms. Los archivos se escriben en la carpeta `Actividades/`.

## Como probar

1. Ejecuta la app.
2. Captura procesos.
3. Pulsa el boton **Simular RF05**.
4. Revisa la carpeta `Actividades/`.

## Pruebas

- Se incluyo un test unitario para validar la escritura de archivos con un directorio temporal.


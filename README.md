# FaceSDKDemo

## Descripción de la solución
FaceSDKDemo es una aplicación Android desarrollada por Alejandro Raya con Jetpack Compose que permite capturar selfies mediante liveness (activo o pasivo), seleccionar imágenes de la galería y comparar la similitud entre ambas utilizando el SDK de Regula FaceSDK.

La aplicación maneja de forma segura los permisos de cámara y almacenamiento, mostrando mensajes claros al usuario si no se conceden. También indica el estado de carga mientras se procesa la comparación facial.

## Patrones utilizados
- **MVVM (Model-View-ViewModel)**
  - ViewModel: maneja la lógica de la vista y expone los estados de UI mediante estados.
  - UI: Jetpack Compose.
  - Model: define los modelos de datos (MainUiState, UiEvent, Result, CaptureMode).

- **Clean Architecture (básico)**
  - Data Layer: acceso al SDK (FaceSdkManager) y manejo de resultados.
  - View Layer: UI y ViewModel.

## Decisiones técnicas destacadas
- Uso de Jetpack Compose para la UI, utilizando las tecnologías más modernas.
- Manejo de cámara y almacenamiento, incluyendo soporte para las diferentes versiones de Android.
- Separación clara entre la interacción con el SDK y la lógica de UI mediante un manager (FaceSdkManager).
- Uso de StateFlow para comunicar estados y eventos de manera reactiva.
- Inicialización del SDK solo si se otorgan los permisos necesarios y deinicialización en onDestroy().

## Aspectos que se podrían mejorar o extender
- Mejor gestión de errores: manejo de timeout o intentos limitados.
- Mejoras en la interfaz de usuario: una UI más atractiva para el usuario, mostrar si la captura es activa o pasiva, editar las propias pantallas del SDK de Regula.
- Pruebas unitarias para ViewModel y simulación de resultados del SDK, o pruebas con la UI.

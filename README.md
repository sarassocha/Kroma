# Kroma Cosmetics S.A.S. — Proyecto Java (MVC + POO + MongoDB)

Plataforma E-commerce en **Java (Spring Boot Web MVC + Thymeleaf + Spring Data MongoDB)** para **Kroma Cosmetics S.A.S.** (*skin-first* y *cruelty-free*), con estética inspirada en **Trendy Shop Colombia**, panel de cliente, panel exclusivo de administrador y pruebas unitarias/integración con JUnit 5.

## Estructura MVC Explícita

- **`model/`** (`src/main/java/com/kroma/ecommerce/model/`):
  - Clases POO con abstracción, herencia, encapsulación y polimorfismo (`EntidadBase`, `Usuario`, `Cliente`, `Administrador`, `Producto`, `DetallePedido`, `Pedido`, `GarantiaTono`, `ToneMatcherService`).
  - `repository/`: Interfaces `MongoRepository` para persistencia en MongoDB.
- **`controller/`** (`src/main/java/com/kroma/ecommerce/controller/`):
  - `BaseController`, `CatalogoController`, `AuthController`, `PedidoController`, `AdminController`.
- **`views/`** (`src/main/resources/views/`):
  - Plantillas **Thymeleaf** + **Tailwind CSS** (`fragments/layout.html`, `client/index.html`, `client/tone-matcher.html`, `client/checkout.html`, `client/mis-pedidos.html`, `auth/login.html`, `auth/registro.html`, `admin/dashboard.html`).

## Cómo Ejecutar en Windows / Apache NetBeans

```powershell
# Compilar y ejecutar pruebas unitarias e integración (4.2)
& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test

# Iniciar la aplicación web en http://localhost:8080
& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" spring-boot:run
```

> **Nota sobre MongoDB:** Si tienes MongoDB local o MongoDB Atlas, configura `kroma.mongodb.uri` en `src/main/resources/application.properties` (o la variable `MONGODB_URI`). Si no hay un servidor MongoDB activo en `127.0.0.1:27017`, `MongoConfig` inicia automáticamente un servidor MongoDB embebido en Java puro (`mongo-java-server`) para que todo funcione sin configuraciones adicionales.

## Credenciales de Acceso

- **Administrador (`/auth/admin-login`):** `admin@kroma.com.co` / `KromaAdmin2026!`
- **Cliente Demo (`/auth/login`):** `camila@ejemplo.com.co` / `Cliente123!`

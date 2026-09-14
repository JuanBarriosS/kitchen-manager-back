<div align="center">

# 🍽️ Kitchen Manager — Backend
### *Sistema de gestión integral para ghost kitchens*

API REST que centraliza pedidos, ventas, menú, empleados y predicción de demanda con inteligencia artificial para operaciones de cocina en tiempo real.

<img src="https://img.shields.io/badge/STATUS-PRODUCTION--READY-brightgreen?style=for-the-badge&logo=checkmarx"/>
<img src="https://img.shields.io/badge/VERSION-1.0.0-blue?style=for-the-badge"/>
<img src="https://img.shields.io/badge/JAVA-21-orange?style=for-the-badge&logo=openjdk"/>
<img src="https://img.shields.io/badge/SPRING_BOOT-3.x-green?style=for-the-badge&logo=springboot"/>
<img src="https://img.shields.io/badge/MONGODB-ATLAS-darkgreen?style=for-the-badge&logo=mongodb"/>


</div>

---

## ⚡ Stack

<div align="center">

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB_Atlas-47A248?style=flat-square&logo=mongodb&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Render](https://img.shields.io/badge/Render-46E3B7?style=flat-square&logo=render&logoColor=black)
![Weka](https://img.shields.io/badge/Weka_J48-9B59B6?style=flat-square)
![GitHub Actions](https://img.shields.io/badge/CI/CD-2088FF?style=flat-square&logo=githubactions&logoColor=white)

</div>

---

## 🚀 Funcionalidades

| Módulo | Descripción |
|--------|-------------|
| 📦 Pedidos | Gestión en tiempo real con estados: recibido → preparación → listo → entregado |
| 📋 Menú | CRUD completo con imágenes y categorías |
| 💰 Ventas | Facturación, historial y exportación a Excel |
| 📱 Portal QR | Clientes hacen pedidos escaneando un QR sin app |
| 🤖 Predicción IA | Modelo J48 (Weka) con 91% de accuracy para predecir demanda por día y hora |
| 👥 Roles | ADMIN y EMPLEADO con rutas protegidas por JWT |

---

## 🔐 Seguridad

- Autenticación con **JWT (HS256)** — expiración de 10 horas
- Contraseñas encriptadas con **BCrypt**
- Control de acceso por roles con **Spring Security**
- CORS configurado para los orígenes autorizados

---

## 📡 Endpoints principales

```
POST   /login                        → Autenticación
GET    /menu/{token}                 → Menú por QR (público)
POST   /menu/{token}/pedido          → Pedido desde QR (público)
GET    /seguimiento/{id}             → Tracking de pedido (público)
GET    /admin/ventas                 → Historial de ventas
GET    /admin/prediccion             → Predicción de demanda IA
GET    /admin/exportar-weka          → Exportar datos ARFF
PATCH  /empleado/pedido/{id}/estado  → Cambiar estado del pedido
POST   /empleado/facturar/{id}       → Facturar pedido
```

---

## ⚙️ Variables de entorno

```env
SPRING_DATA_MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/restaurante
JWT_SECRET=tu_clave_secreta
```

---

## 🛠️ Ejecutar localmente

```bash
git clone https://github.com/JuanBarriosS/kitchen-manager-back
cd kitchen-manager-back
mvn spring-boot:run
```

> Configura las variables de entorno antes de ejecutar.

---

## 🌐 Producción

| Servicio | URL |
|----------|-----|
| Backend | `https://kitchen-manager-back.onrender.com` |
| Frontend | `https://kitchen-manager-front.vercel.app` |

---

<div align="center">

**Juan Barrios · Hamlet Cuadro · José Miranda**  
Tecnológico Comfenalco · Ingeniería de Sistemas · 2026

</div>

# Receipt Processor

A Spring Boot application that processes receipts and calculates reward points based on specific business rules. This project features RESTful endpoints, integration with an in-memory H2 database, Docker containerization, and comprehensive testing using JUnit and Google Truth.

## Features

- **Process Receipt:** Accepts a receipt in JSON format, stores it in the database, and prevents duplicate submissions based on retailer, purchase date, and purchase time.
- **Calculate Points:** Computes reward points from the receipt details using defined business logic.
- **Response Wrapping:** Uses custom DTOs (`ReceiptIdResponse` and `PointsResponse`) to standardize API responses.
- **Testing:** Unit tests for the service and controller layers using JUnit and Google Truth.
- **Docker:** Multi-stage Dockerfile for building and running the application in a container.

## Prerequisites

- **Java:** JDK 17
- **Build Tool:** Maven
- **Docker:** Docker Desktop (optional, for containerization)
- **Git:** For source control

## Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd receipt-processor
```

### Build the Application

Build using Maven:

```bash
mvn clean package
```

This will compile the project and generate the executable JAR in the `target` directory.

### Running Locally

#### Using Maven

Start the application with:

```bash
mvn spring-boot:run
```

#### Using the JAR

Run the packaged JAR file:

```bash
java -jar target/receipt-processor-<version>.jar
```

The application will run on port **8080** by default.

### API Endpoints

- **Process Receipt**
    - **Endpoint:** `POST /receipts/process`
    - **Request Body:** A JSON object representing a receipt.
    - **Response:** A JSON object with the property `"id"` containing the receipt ID.

- **Calculate Points**
    - **Endpoint:** `GET /receipts/{id}/points`
    - **Response:** A JSON object with the property `"points"` containing the calculated reward points.

## Testing

### Unit Tests

Run the tests using Maven:

```bash
mvn test
```

Test cases are written for both the service and controller layers. They use Google Truth for assertions and load test data from JSON files located in `src/test/resources`.

## Docker

### Building the Docker Image

Ensure Docker Desktop is installed and running on your Mac, then build the image from the project root:

```bash
docker build -t my-receipt-processor .
```

### Running the Docker Container

Run the container and map port 8080:

```bash
docker run -p 8080:8080 my-receipt-processor
```

Access the application at [http://localhost:8080](http://localhost:8080).

## Contributing

Contributions are welcome! Please fork this repository and open a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

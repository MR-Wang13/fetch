# Receipt Processor

A Spring Boot application that processes receipts and calculates reward points based on specific business rules. This project features RESTful endpoints, integration with an in-memory H2 database, Docker containerization, and comprehensive testing using JUnit and Google Truth.

## Features

- **Process Receipt:** Accepts a receipt in JSON format, stores it in the database, and prevents duplicate submissions based on retailer, purchase date, and purchase time.
- **Calculate Points:** Computes reward points from the receipt details using defined business logic.
- **Response Wrapping:** Uses custom DTOs (`ReceiptIdResponse` and `PointsResponse`) to standardize API responses.
- **Testing:** Unit tests for the service and controller layers using JUnit and Google Truth, with JSON test cases stored in `src/test/resources`.
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

## API Endpoints

- **Process Receipt**
  - **Endpoint:** `POST /receipts/process`
  - **Request Body:** A JSON object representing a receipt.
  - **Response:** A JSON object with the property `"id"` containing the receipt ID.

- **Calculate Points**
  - **Endpoint:** `GET /receipts/{id}/points`
  - **Response:** A JSON object with the property `"points"` containing the calculated reward points.

## JSON Test Cases

Several JSON test case files are included in the `src/test/resources` directory to simulate different scenarios for unit testing:

- **testReceipt.json:** A basic receipt for standard testing.
- **testReceipt_AllRules.json:** A receipt that triggers all reward point rules (expected points: 113).
- **testReceipt_NoTimeBonus.json:** A receipt that does not qualify for the time bonus rule (expected points: 20).

These files are used by the unit tests to verify that the business logic works as expected.

## Postman Test Cases

A Postman collection is provided to test the API endpoints. You can import the collection into Postman using the following URL:

[Postman Collection - Receipt Processor API](https://elements.getpostman.com/redirect?entityId=31906088-0206804c-40ea-42b4-b590-1f6f608aba7e&entityType=collection)

After importing the collection, you can run the test cases to verify the API's functionality.

## Deployed Application

You can test the deployed version of the application at the following URL:

[http://receiptprocessor-env.eba-t22vhcxy.us-east-1.elasticbeanstalk.com/](http://receiptprocessor-env.eba-t22vhcxy.us-east-1.elasticbeanstalk.com/)

## Testing

### Unit Tests

Run the tests using Maven:

```bash
mvn test
```

Test cases are written for both the service and controller layers using JUnit and Google Truth. The tests use the JSON files in `src/test/resources` to simulate various input scenarios.

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

# Welcome to My Currency Conversion App (Backend)

The idea of this project was to implement a backend to consume an external API and keep a database updated with real-time exchange rates for various currencies.

The external API used was: https://docs.awesomeapi.com.br/api-de-moedas

> **Note 1:** The possible conversions are only to the **Brazilian Real (BRL)** currency.

> **Note 2:** To optimize database space, the application only retrieves new currency quotes during business days and business hours. This can be changed in the Scheduler configured in the **CurrencyService.java** class.

The project used Docker to host the basic infrastructure, which includes: **PostgreSQL** database and in-memory **Redis** database.

To test the project, follow the steps below:

- Run *git clone*
- Navigate to the *cloned-directory*
- Run command: *docker-compose up -d*
- Run command: *./mvnw spring-boot:run*

If you wish to understand better the API documentation, please, access the path below with the application running:

[localhost:8080/swagger-ui/index.html#/](localhost:8080/swagger-ui/index.html#/)

If you want to see the complete app working I have also left the frontend project here on my GitHub:

Repositorie: [Frontend-CurrencyQuote](https://github.com/FernandoAvellar/Frontend-CurrencyQuote)

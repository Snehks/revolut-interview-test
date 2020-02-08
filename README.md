#Revolut Backend Test

## Tech Choices Notes
1. Programming language used is Java version 11
2. Gradle is used as the build system
3. Libraries used as as follow
    - JavaSpark - Framework for REST
    - Guice - Dependency Injection
    - Hibernate - ORM
    - H2 - Database
    - Junit 5 & Mockito - For testing and mocking

## Implementation Notes
1. The solution implemented assumes that the account can have just a `Single Currency` stored in the account table. Extension is possible in future.
2. Some of the Implemented database queries (like get all transactions) are not optimised for `HUGE` amounts of data. This can be improved in future.
3. The solution has 3 different `Resources`
    - Account - To create & retrieve accounts.
    - Transfer - To initiate a transfer between 2 accounts.
    - Transaction - To view all transactions and their states for a given account.

## How to run
1. Build
    - Linux - `./gradlew clean build`
    - Windows - `gradlew.bat clean build`
2.  Run (Default port used is `8000`)
    - Linux - `./gradlew clean run`
    - Windows - `gradlew.bat clean run`

## Endpoints
Once the server is running use the following end points to interact with the server.

1. Accounts
    - Get account by id - `GET` - `localhost:8000/api/account/{accountId}`
    - Create an account - `POST` - `localhost:8000/api/account/` with body
    ```json
    
   {
       "id": null,
       "balance": 1
   }
   
    ```
2. Transfers
    - Transfer money `POST` - `localhost:8000/api/account/` with body                            
    ```json
   {
   	"senderId": 1,
   	"receiverId": 2,
   	"amountToTransfer": 0.5
   }
   ```
   This returns an `OK` response when transfer gets queued. The transfer can be executed anytime in the future. 
   It is not synchronous. This has been done to allow the transfer model to work across multiple servers if needed. 
   **_For the current implementation, it just runs the transfer on the same thread. This can be easily changed by
   updating the executor type in `TransactionModule`._** 
    
3. Transactions
    - Get all the transactions for account id `GET` - `localhost:8000/transactions/{accountId}`
    
## Endpoints

1. Unit Tests
    - Linux - `./gradlew clean test`
    - Windows - `gradlew.bat clean test`
    
2. Integration Tests
    - Linux - `./gradlew clean iT`
    - Windows - `gradlew.bat clean iT`

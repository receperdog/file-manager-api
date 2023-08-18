FileManager API Documentation

File Management API
Base URL: /api/files

1. Retrieve all files:
   Endpoint: GET /
   Description:
   Get a list of all File meta data.

2. Retrieve a File by Id:
   Endpoint: GET /{id}
   Description:
   Get a File meta data object by specifying its id.

3. Upload a file:
   Endpoint: POST /upload
   Description:
   Upload a file and save its meta data.

4. Download a file by its Id:
   Endpoint: GET /download/{id}
   Description:
   Retrieve the actual content of a file given its meta data ID.

5. Delete a file meta data by its Id:
   Endpoint: DELETE /{id}
   Description:
   Delete the meta data information of a file given its ID.

6. Update file and its meta data by its Id:
   Endpoint: PUT /{id}
   Description:
   Update the content and meta data of a file given its ID.

User Authentication API
Base URL: /api/v1/auth

1. Signup:
   Endpoint: POST /signup
   Description:
   Register a new user.

2. Signin:
   Endpoint: POST /signin
   Description:
   Authenticate a user.
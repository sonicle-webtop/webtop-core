## Update Notes

1. Download and unpack Swagger UI package from https://github.com/swagger-api/swagger-ui/releases into dedicated folder

2. Download and unpack themes for Swagger UI from https://github.com/ostranme/swagger-ui-themes/releases into `themes` subfolder

3. Set title of page `index.html` to `WebTop - Swagger UI` 

4. Adds the HTML code below into HEAD of `index.html` page and after directive pointing to `swagger-ui.css`

   ```
   <link rel="stylesheet" type="text/css" href="./themes/theme-flattop.css">
   ```

5. Update JS code in `swagger-initializer.js` adding just after `SwaggerUIBundle` object definition

   ```
   configUrl: "./swagger-config.json",
   ```

6. Finally update Java Servlet's init parameter in order to reach current folders path


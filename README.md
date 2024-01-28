# java-scraping-no-api

Essa é uma API Java, desenvolvida utilizando Spring Boot e Maven. A API tem como objetivo realizar um web scraping em um repositório do GitHub, analisar todos os arquivos presentes no repositório e retornar todas as extensões presentes no repositório, a quantidade de arquivos existentes para cada extensão, a quantidade total de linhas nesses arquivos, e o tamanho total dos arquivos em bytes. Esse web scraping é feito sem a utilização de nenhuma API, como por exemplo o JSoup.

A api está hospedada na seguinte URL: https://java-scraping-no-api-2464b5478565.herokuapp.com

### Rotas
#### /git-info?rep={{url do repositório}} - Método: GET
a url do repositório deve ser {{usuário}}/{{nome do rep}}, é importante que não seja inserida a barra ("/") nem no inicio nem no final da declaração da url

Exemplo de rota: https://java-scraping-no-api-2464b5478565.herokuapp.com/git-info?rep=gabrielgcosta/botcopa

Retorno da rota:

![image](https://github.com/gabrielgcosta/java-scraping-no-api/assets/42680760/7f1217aa-d2a5-4d31-8b76-dfba69b647df)




--------------------------------------


This is a Java API developed using Spring Boot and Maven. The API's purpose is to perform web scraping on a GitHub repository, analyze all files in the repository, and return information about the extensions present in the repository. This includes the number of files for each extension, the total number of lines in these files, and the overall size of the files in bytes. The web scraping is done without the use of any external API, such as JSoup.

The API is hosted at the following URL: https://java-scraping-no-api-2464b5478565.herokuapp.com

Routes:

/git-info?rep={{repository URL}} - Method: GET

The repository URL should be in the format {{user}}/{{repository name}}. It's important not to include a slash ("/") at the beginning or end of the URL declaration.

Example route: https://java-scraping-no-api-2464b5478565.herokuapp.com/git-info?rep=gabrielgcosta/botcopa

Route response:

![image](https://github.com/gabrielgcosta/java-scraping-no-api/assets/42680760/7f1217aa-d2a5-4d31-8b76-dfba69b647df)

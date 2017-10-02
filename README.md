шаги, необходимые для запуска бота:

1) создать google форму. для теста я уже создал эту :
`https://docs.google.com/forms/d/1hG1ac68fMbjDeQ1jERQn0CRNjy6hkOknFgZbqac9lz4/edit?usp=sharing`

можно использовать ее. в противном случае придется заполнить блок
```googleForm``` в `application.json`.

гугл не предоставляет открытого Rest API для создания форм (только апи для Google Apps Script и по крайней мере здесь sdk нет `https://developers.google.com/api-client-library/java/apis/` или я в глаза долблюсь)

поэтому вся отправка через выдранные из POST запроса данные :
  ```"googleForm":{
    "formUri":"https://docs.google.com/forms/d/e/1FAIpQLSfpWAWsNEZ4G0pbtF-XNE3arwNlYn-e5Th5BTnp0L2PD6OslQ/formResponse",
    "bio": "entry.695648163",
    "phoneNumber": "entry.789588282",
    "email" : "entry.1187330536",
    "workStack" : "entry.1757632305",
    "ownStack" : "entry.1885265433",
    "minRate" : "entry.1004429887",
    "prefferedRate" : "entry.202934781",
    "timeZone" : "entry.960613366",
    "freeHours" : "entry.1212794204",
    "tgLink" : "entry.270212300"
  }
```

2) не считая этих настроек запуск может быть в двух вариантах:
- sbt dockerComposeUp
запустит экземпляр бота и mongo

- TELEGRAM_KEY=<ключ от бота> sbt run
(предварительно нужно поставить mongoDB)

ключ для бота : `478141275:AAFq1n2J5oYmh0Ra6I6HJZQD5xy5zgghwao`


работа с ботом :
при первом добавлении бота он предложит ввести ФИО и далее по списку.

далее , для бота есть команда /update с помощью которой можно обновить любое из полей.

раз в 2 недели бот спрашивает пользователя о намерении обновить рабочие часы. Параметр частоты запроса захардкожен в `me/luger/dicoiner/bot/services/BotServise.scala`
в поле
   `private val twoWeeksSeconds = 2 * 7 * 24 * 60`
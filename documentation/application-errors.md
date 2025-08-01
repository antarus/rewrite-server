# RewriteServer errors

RewriteServer errors are meant to be specialized and internationalized. That mean you'll have to create dedicated exceptions extending `RewriteServerException`, example:

```java
class UnknownFileToDeleteException extends RewriteServerException {

  public UnknownFileToDeleteException(String file) {
    super(badRequest(FilesErrorKey.UNKNOWN_FILE_TO_DELETE).message(buildMessage(file)).addParameter("file", file.get()));
  }

  private static String buildMessage(String file) {
    return new StringBuilder().append("File to delete ").append(file).append(", can't be found").toString();
  }
}

```

With `FilesErrorKey`:

```java
enum FilesErrorKey implements ErrorKey {
  UNKNOWN_FILE_TO_DELETE("unknown-file-to-delete");

  private final String key;

  ModuleSecondaryErrorKey(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }
}

```

And updates to all messages files in `src/main/resources/messages/errors` to add:

- `error.unknown-file-to-delete.title`: With error title following [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) title
- `error.unknown-file-to-delete.detail`: With error detail following [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) title. Here you can use mustaches ({{ and }}) with parameters in your exception (here {{ file }})

This may sound like a lot of work but that way you'll:

- Have really clear stack traces whenever a problem occurs;
- Never have a missing translation (this is tested);
- Won't have to sync client <-> server deployment since client just have to display a message sent by the server.

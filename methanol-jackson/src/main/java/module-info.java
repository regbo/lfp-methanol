module methanol.convert.jackson {
  requires transitive methanol;
  requires transitive com.fasterxml.jackson.databind;
  requires org.checkerframework.checker.qual;

  exports com.github.mizosoft.methanol.convert.jackson;
}

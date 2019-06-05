package com.github.daggerok;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AppVintageTest {

  @Test
  public void main() {
    var ctx = new AnnotationConfigApplicationContext(App.class);
    assertThat(ctx).isNotNull();
  }
}

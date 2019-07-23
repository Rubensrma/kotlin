// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.gradle.tooling.serialization;

import com.amazon.ion.IonReader;
import com.amazon.ion.IonType;
import com.amazon.ion.IonWriter;
import com.intellij.openapi.util.Getter;
import com.intellij.util.ThrowableConsumer;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.model.ExternalDependency;
import org.jetbrains.plugins.gradle.tooling.serialization.ExternalProjectSerializationService.ReadContext;
import org.jetbrains.plugins.gradle.tooling.serialization.ExternalProjectSerializationService.WriteContext;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Vladislav.Soroka
 */
public class ToolingStreamUtils {

  @Nullable
  public static String readString(@NotNull IonReader reader, @Nullable String fieldName) {
    IonType type = reader.next();
    assert fieldName == null || fieldName.equals(reader.getFieldName());
    if (type == null) return null;
    return reader.stringValue();
  }

  public static int readInt(@NotNull IonReader reader, @NotNull String fieldName) {
    reader.next();
    assert fieldName.equals(reader.getFieldName());
    return reader.intValue();
  }

  public static boolean readBoolean(@NotNull IonReader reader, @NotNull String fieldName) {
    reader.next();
    assert fieldName.equals(reader.getFieldName());
    return reader.booleanValue();
  }

  public static List<File> readFiles(@NotNull IonReader reader) {
    reader.next();
    List<File> list = new ArrayList<File>();
    reader.stepIn();
    File file;
    while ((file = readFile(reader, null)) != null) {
      list.add(file);
    }
    reader.stepOut();
    return list;
  }

  public static Set<File> readFilesSet(@NotNull IonReader reader) {
    reader.next();
    Set<File> set = new THashSet<File>();
    reader.stepIn();
    File file;
    while ((file = readFile(reader, null)) != null) {
      set.add(file);
    }
    reader.stepOut();
    return set;
  }

  @Nullable
  public static File readFile(@NotNull IonReader reader, @Nullable String fieldName) {
    String filePath = readString(reader, fieldName);
    return filePath == null ? null : new File(filePath);
  }


  public static <K, V> Map<K, V> readMap(@NotNull IonReader reader,
                                         @NotNull Getter<? extends K> keyReader,
                                         @NotNull Getter<? extends V> valueReader) {
    reader.next();
    reader.stepIn();
    Map<K, V> map = new THashMap<K, V>();
    while (reader.next() != null) {
      reader.stepIn();
      map.put(keyReader.get(), valueReader.get());
      reader.stepOut();
    }
    reader.stepOut();
    return map;
  }

  public static Map<String, Set<File>> readStringToFileSetMap(@NotNull final IonReader reader) {
    return readMap(reader, new Getter<String>() {
      @Override
      public String get() {
        return readString(reader, null);
      }
    }, new Getter<Set<File>>() {
      @Override
      public Set<File> get() {
        return readFilesSet(reader);
      }
    });
  }

  public static void writeString(@NotNull IonWriter writer, @NotNull String fieldName, @Nullable String value) throws IOException {
    writer.setFieldName(fieldName);
    writer.writeString(value);
  }

  public static void writeFile(@NotNull IonWriter writer, @NotNull String fieldName, @Nullable File file) throws IOException {
    writeString(writer, fieldName, file == null ? null : file.getPath());
  }

  public static <K, V> void writeMap(@NotNull IonWriter writer,
                                     @NotNull String fieldName,
                                     @NotNull Map<K, V> map,
                                     @NotNull ThrowableConsumer<? super K, ? extends IOException> keyWriter,
                                     @NotNull ThrowableConsumer<? super V, ? extends IOException> valueWriter)
    throws IOException {
    writer.setFieldName(fieldName);
    writer.stepIn(IonType.LIST);
    for (Map.Entry<K, V> entry : map.entrySet()) {
      writer.stepIn(IonType.STRUCT);
      writer.setFieldName("key");
      keyWriter.consume(entry.getKey());
      writer.setFieldName("value");
      valueWriter.consume(entry.getValue());
      writer.stepOut();
    }
    writer.stepOut();
  }

  public static void writeFiles(@NotNull IonWriter writer, @NotNull String fieldName, @NotNull Collection<File> files) throws IOException {
    writer.setFieldName(fieldName);
    writer.stepIn(IonType.LIST);
    for (File file : files) {
      writer.writeString(file.getPath());
    }
    writer.stepOut();
  }

  public static Set<String> readStringSet(@NotNull IonReader reader) {
    Set<String> set = new THashSet<String>();
    reader.next();
    reader.stepIn();
    String nextString;
    while ((nextString = readString(reader, null)) != null) {
      set.add(nextString);
    }
    reader.stepOut();
    return set;
  }

  @SuppressWarnings("unused")
  public static ExternalDependency readDependency(@NotNull IonReader reader, @NotNull ReadContext context) {
    return ExternalProjectSerializationService.readDependency(reader, context);
  }

  @SuppressWarnings("unused")
  public static void writeDependency(@NotNull IonWriter writer,
                                     @NotNull WriteContext context,
                                     @NotNull ExternalDependency dependency) throws IOException {
    ExternalProjectSerializationService.writeDependency(writer, context, dependency);
  }
}

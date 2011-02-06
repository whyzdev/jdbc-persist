/*
 * Copyright 2011 Colin Decker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cgdecker.guice.jdbc;

import com.google.common.base.Objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
class Foo {
  private final int id;
  private final String name;

  Foo(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override public boolean equals(Object obj) {
    if (!(obj instanceof Foo)) return false;
    Foo other = (Foo) obj;
    return id == other.id && Objects.equal(name, other.name);
  }

  @Override public int hashCode() {
    return Objects.hashCode(id, name);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .toString();
  }

  static List<Foo> fromResultSet(ResultSet rS) throws SQLException {
    List<Foo> result = new ArrayList<Foo>();
    while (rS.next()) {
      int id = rS.getInt("id");
      String name = rS.getString("name");
      result.add(new Foo(id, name));
    }
    return result;
  }
}

/*
 * Dateiname: QueryResultsWithSchema.java
 * Projekt  : WollMux
 * Funktion : Ein Container für Ergebnisse einer Datenbankafrage zusammen mit dem zugehörigen Schema.
 * 
 * Copyright (c) 2008 Landeshauptstadt München
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the European Union Public Licence (EUPL),
 * version 1.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 *
 * You should have received a copy of the European Union Public Licence
 * along with this program. If not, see
 * http://ec.europa.eu/idabc/en/document/7330/5980
 *
 * Änderungshistorie:
 * Datum      | Wer | Änderungsgrund
 * -------------------------------------------------------------------
 * 04.03.2008 | BNK | Erstellung
 * -------------------------------------------------------------------
 *
 * @author Matthias Benkmann (D-III-ITD D.10)
 * @version 1.0
 * 
 */
package de.muenchen.allg.itd51.wollmux.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Ein Container für Ergebnisse einer Datenbankafrage zusammen mit dem zugehörigen
 * Schema.
 * 
 * @author Matthias Benkmann (D-III-ITD D.10)
 */
public class QueryResultsWithSchema implements QueryResults
{
  protected QueryResults results;

  protected Set<String> schema;

  /**
   * Constructs an empty QueryResultsWithSchema with empty schema.
   */
  public QueryResultsWithSchema()
  {
    results = new QueryResultsList(new ArrayList<Dataset>());
    schema = new HashSet<String>();
  }

  /**
   * Erzeugt ein neues QueryResultsWithSchema, das den Inhalt von res und das Schema
   * schema zusammenfasst. ACHTUNG! res und schema werden als Referenzen übernommen.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public QueryResultsWithSchema(QueryResults res, Set<String> schema)
  {
    this.schema = schema;
    this.results = res;
  }

  public int size()
  {
    return results.size();
  }

  public Iterator<Dataset> iterator()
  {
    return results.iterator();
  }

  public boolean isEmpty()
  {
    return results.isEmpty();
  }

  public Set<String> getSchema()
  {
    return new HashSet<String>(schema);
  }

}

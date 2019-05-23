// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.storage;

import android.support.annotation.Nullable;
import com.google.android.gms.common.internal.Preconditions;
import com.google.firebase.annotations.PublicApi;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A single paged result of a {@link StorageReference#list} call. */
@PublicApi
public class ListResult {
  private static final String PAGE_TOKEN_KEY = "nextPageToken";
  private static final String PREFIXES_KEY = "prefixes";
  private static final String ITEMS_KEY = "items";

  private List<StorageReference> prefixes;
  private List<StorageReference> items;
  @Nullable private String pageToken;

  ListResult(
      List<StorageReference> prefixes, List<StorageReference> items, @Nullable String pageToken) {
    this.prefixes = prefixes;
    this.items = items;
    this.pageToken = pageToken;
  }

  /** The prefixes (subdirectories) returned by the {@code List()} operation. */
  @PublicApi
  public List<StorageReference> getPrefixes() {
    return prefixes;
  }

  /** The items (files) returned by the {@code List()} operation. */
  @PublicApi
  public List<StorageReference> getItems() {
    return items;
  }

  /**
   * Returns a token that can be used to resume a previous {@code List()} operation. `null`
   * indicates that there are no more results
   */
  @PublicApi
  @Nullable
  public String getPageToken() {
    return pageToken;
  }

  static class Builder {
    private final JSONObject resultBody;
    private final StorageReference storageRef;
    private final FirebaseStorage firebaseStorage;

    public Builder(JSONObject resultBody, StorageReference storageRef) {
      Preconditions.checkNotNull(resultBody);

      this.resultBody = resultBody;
      this.storageRef = storageRef;
      this.firebaseStorage = storageRef.getStorage();
    }

    ListResult build() throws JSONException {

      List<StorageReference> prefixes = new ArrayList<>();
      List<StorageReference> items = new ArrayList<>();

      if (resultBody.has(PREFIXES_KEY)) {
        JSONArray prefixEntries = resultBody.getJSONArray(PREFIXES_KEY);
        for (int i = 0; i < prefixEntries.length(); ++i) {
      String pathWithoutTrailingSlash = prefixEntries.getString(i);
      if (pathWithoutTrailingSlash.endsWith("/")) {
        pathWithoutTrailingSlash = pathWithoutTrailingSlash.substring(0, pathWithoutTrailingSlash.length() - 1);
      }
          prefixes.add(firebaseStorage.getReference(pathWithoutTrailingSlash));
        }
      }

      if (resultBody.has(ITEMS_KEY)) {
        JSONArray itemEntries = resultBody.getJSONArray(ITEMS_KEY);
        for (int i = 0; i < itemEntries.length(); ++i) {
          JSONObject metadata = itemEntries.getJSONObject(i);

          items.add(firebaseStorage.getReference(metadata.getString("name")));
        }
      }

      String pageToken = resultBody.optString(PAGE_TOKEN_KEY, null);
      return new ListResult(prefixes, items, pageToken);
    }
  }
}
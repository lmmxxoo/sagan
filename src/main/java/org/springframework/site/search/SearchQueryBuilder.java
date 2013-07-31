package org.springframework.site.search;

import io.searchbox.core.Search;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public class SearchQueryBuilder {


	private static final String emptyQuery =
			"  \"query\": {\n" +
					"    \"bool\": {\n" +
					"      \"should\": [{\n" +
					"        \"match_all\": {}\n" +
					"      }]\n" +
					"    }\n" +
					"  }";

	private static final String fullQuery =
			"\"query\": {\n" +
					"  \"custom_score\": {\n" +
					"    \"query\": {\n" +
					"      \"multi_match\": {\n" +
					"        \"query\": \"%s\",\n" +
					"        \"fields\": [\n" +
					"          \"title^10\",\n" +
					"          \"rawContent\"\n" +
					"        ]\n" +
					"      }\n" +
					"    },\n" +
					"    \"script\": \"_score * (_source.current ? 1.1 : 1.0)\"\n" +
					"  }\n" +
					"}";

	private static final String rawQueryFilters =
			"\"filter\": {\n" +
					"      \"range\": {\n" +
					"        \"publishAt\": {\n" +
					"          \"from\": \"\",\n" +
					"          \"to\": \"%s\",\n" +
					"          \"include_lower\": true,\n" +
					"          \"include_upper\": true\n" +
					"        }\n" +
					"      }\n" +
					"    }\n";

	private static final String highlight =
			"\"highlight\": {\n" +
					"    \"order\": \"score\",\n" +
					"    \"require_field_match\": false,\n" +
					"    \"fields\": {\n" +
					"      \"rawContent\": {\n" +
					"        \"fragment_size\": 300,\n" +
					"        \"number_of_fragments\": 1\n" +
					"      }\n" +
					"    }\n" +
					"  }";

	private static final String facets = "\"facets\" : {\n" +
			"  \"facet_paths_result\" : {\n" +
			"   \"terms\" : {\n" +
			"    \"field\" : \"facetPaths\",\n" +
			"    \"order\" : \"term\",\n" +
			"    \"size\" : 50\n" +
			"   }\n" +
			"  }\n" +
			" }";

	SearchQueryBuilder() {
	}

	Search.Builder forEmptyQuery(Pageable pageable) {
		return new Search.Builder("{" + emptyQuery + ","
				+ buildQueryPagination(pageable) + ","
				+ highlight + ","
				+ facets
				+ "}");
	}

	Search.Builder forQuery(String queryTerm, Pageable pageable) {
		return new Search.Builder("{" + buildFullQuery(queryTerm) + ","
				+ buildQueryFilters(new Date()) + ","
				+ buildQueryPagination(pageable) + ","
				+ highlight + ","
				+ facets
				+ "}");
	}

	private String buildFullQuery(String queryTerm) {
		return String.format(fullQuery, queryTerm, queryTerm);
	}

	private String buildQueryFilters(Date toDate) {
		String formattedDate = ISODateTimeFormat.dateTimeNoMillis().print(toDate.getTime());
		return String.format(rawQueryFilters, formattedDate);
	}

	private String buildQueryPagination(Pageable pageable) {
		return String.format("\"from\":%d,\"size\":%d", pageable.getOffset(), pageable.getPageSize());
	}
}
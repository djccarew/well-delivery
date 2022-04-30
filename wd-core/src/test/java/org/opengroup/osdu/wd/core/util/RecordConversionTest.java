// Copyright 2020 Schlumberger
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

package org.opengroup.osdu.wd.core.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.client.model.http.DpsHeaders;
import org.opengroup.osdu.core.client.model.storage.Record;
import org.opengroup.osdu.wd.core.auth.RequestInfo;
import org.opengroup.osdu.wd.core.models.ACL;
import org.opengroup.osdu.wd.core.models.EntityDto;
import org.opengroup.osdu.wd.core.models.Legal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RecordConversionTest {

    private static final String HEADER_DATA_PARTITION_ID = "anyDataPartitionId";

    @Mock
    RequestInfo requestInfo;

    @InjectMocks
    RecordConversion sut;

    private DpsHeaders headers;

    @Before
    public void setup(){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(DpsHeaders.DATA_PARTITION_ID, HEADER_DATA_PARTITION_ID);
        this.headers = DpsHeaders.createFromMap(headerMap);
    }


    @Test
    public void should_returnRecord_when_DtoValid() {
        String ddmsId = "ddmsId-test";
        ReflectionTestUtils.setField(this.sut, "ddmsId", ddmsId);

        when(this.requestInfo.getDpsHeaders()).thenReturn(headers);
        EntityDto dto = new EntityDto(
                "id-test",
                "entityid-test",
                "entityType-test",
                "kind-test",
                5435435,
                new Legal(),
                new ACL(),
                "planned",
                true,
                "2021-9-1",
                "2021-9-1",
                "2021-11-1",
                "test",
                "meta"
        );
        dto.getAcl().setOwners(new HashSet<>(Arrays.asList("owner-test1", "owner-test2")));
        dto.getAcl().setViewers(new HashSet<>(Arrays.asList("viewer-test1", "viewer-test2")));
        dto.getLegal().setLegaltags(new HashSet<>(Arrays.asList("legal-test1", "legal-test1")));
        dto.getLegal().setOtherRelevantDataCountries(new HashSet<>(Arrays.asList("country-test1", "country-test1")));
        Record actual = this.sut.toRecord(dto);
        assertEquals(HEADER_DATA_PARTITION_ID + ":wd-ddms:" + dto.getEntityId(), actual.getId());
        assertEquals(dto.getKind(), actual.getKind());
        assertEquals(dto.getVersion(), (long) actual.getVersion());
        assertEquals(dto.getAcl().getViewers().size(), actual.getAcl().getViewers().length);
        assertEquals(dto.getAcl().getOwners().size(), actual.getAcl().getOwners().length);
        for (String viewer : actual.getAcl().getViewers()) {
            assertTrue(dto.getAcl().getViewers().contains(viewer));
        }
        for (String owner : actual.getAcl().getOwners()) {
            assertTrue(dto.getAcl().getOwners().contains(owner));
        }
        assertEquals(dto.getLegal().getLegaltags().size(), actual.getLegal().getLegaltags().size());
        for (String legalTag : dto.getLegal().getLegaltags()) {
            assertTrue(actual.getLegal().getLegaltags().contains(legalTag));
        }
        assertEquals(dto.getLegal().getOtherRelevantDataCountries().size(), actual.getLegal().getOtherRelevantDataCountries().size());
        for (String country : dto.getLegal().getOtherRelevantDataCountries()) {
            assertTrue(actual.getLegal().getOtherRelevantDataCountries().contains(country));
        }
        assertEquals(dto.getEntityId(), actual.getData().get("localid").getAsString());
        assertEquals(dto.getEntityType(), actual.getData().get("entityType").getAsString());
        assertEquals(ddmsId, actual.getData().get("ddmsid").getAsString());
    }
}

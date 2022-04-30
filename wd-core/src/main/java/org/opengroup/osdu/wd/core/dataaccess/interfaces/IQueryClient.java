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

package org.opengroup.osdu.wd.core.dataaccess.interfaces;


import org.opengroup.osdu.wd.core.models.EntityDtoReturn;

import java.util.Date;
import java.util.List;

public interface IQueryClient {

    //BHARun
    List<EntityDtoReturn> getBHARunsBySection(String existenceKind, String sectionId);
    List<EntityDtoReturn> getBHARunsByWells(String existenceKind, List<String> wellIds);
    List<EntityDtoReturn> getBHARunsByWellbore(String existenceKind, String wellboreId);

    //HoleSection
    List<EntityDtoReturn> getHoleSectionsByWellbore(String existenceKind, String wellboreId);

    //WellboreTrajectory
    List<EntityDtoReturn> getWellboreTrajectoriesByWells(String existenceKind, List<String> wellIds);

    //ActivityPlan
    EntityDtoReturn getLatestActivityPlanByWell(String existenceKind, String wellId);

    //WellActivityProgram
    EntityDtoReturn getLatestWellActivityProgramByWell(String existenceKind, String wellId);
    EntityDtoReturn getSpecificWellActivityProgramByWell(String existenceKind, String wellId, long dpVersion);
    Object getLatestWellActivityProgramRefTreeByWell(String existenceKind, String wellId);
    Object getSpecificWellActivityProgramRefTreeByWell(String existenceKind, String wellId, long dpVersion);
    List<Long> getWellActivityProgramVersionNumbersByWell(String existenceKind, String wellId);
    List<EntityDtoReturn> getLatestWellActivityProgramChildrenListByWell(String existenceKind, String wellId);

    //Wellbore
    List<Long> getWellboreVersionNumbersByWell(String existenceKind, String wellId);
    EntityDtoReturn getLatestWellboreVersionByWell(String existenceKind, String wellId);
    EntityDtoReturn getSpecificWellboreVersionByWell(String existenceKind, String wellId, long wellboreVersion);

    //Well
    List<Long>  getWellVersionNumbers(String existenceKind, String name);
    EntityDtoReturn getLatestWellVersion(String existenceKind, String name);
    EntityDtoReturn getSpecificWellVersion(String existenceKind, String name, long version);

    //DrillingReport
    List<EntityDtoReturn> getDrillingReportsByWellbore(String existenceKind, String wellboreId);
    EntityDtoReturn getLatestDrillingReportByWellbore(String existenceKind, String wellboreId);
    List<EntityDtoReturn> getDrillingReportsByTimeRange(String existenceKind, String startTimeString, String endTimeString);
    Object getLatestDrillingReportRefTree(String existenceKind, String drillingReportId);

    //FluidsReport
    List<EntityDtoReturn> getFluidsReportsByWellbore(String existenceKind, String wellboreId);
}

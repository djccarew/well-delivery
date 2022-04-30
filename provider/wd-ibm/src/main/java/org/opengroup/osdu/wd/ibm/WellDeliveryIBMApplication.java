/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.wd.ibm;

import org.opengroup.osdu.wd.core.dataaccess.impl.MongodbInit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.FilterType;



@ComponentScan(value = {
        "org.opengroup.osdu.wd",
        "org.opengroup.osdu.core"
},
        excludeFilters = @Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = MongodbInit.class
        )
)
@SpringBootApplication(exclude={
        MongoAutoConfiguration.class
})

@EnableAsync
public class WellDeliveryIBMApplication {

	 public static void main(String[] args) {
	        Class<?>[] sources = new Class<?>[]{
	        	WellDeliveryIBMApplication.class

	        };
	        SpringApplication.run(sources, args);
	    }
}

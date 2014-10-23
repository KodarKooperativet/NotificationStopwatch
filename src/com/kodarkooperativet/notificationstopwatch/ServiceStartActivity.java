package com.kodarkooperativet.notificationstopwatch;

import com.kodarkooperativet.notificationstopwatch.TimeService.TimeContainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 *    Copyright 2013 KodarKooperativet
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 * Only used for Starting/Stopping the Service. 
 * 
 * This Activity is invisible for the user.
 * 
 * @author KodarKooperativet
 *
 */
public class ServiceStartActivity extends Activity {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = new Intent(this, TimeService.class);
        if(TimeContainer.getInstance().isServiceRunning.get()) {
        	stopService(intent);
        } else {
        	startService(intent);
        }
        finish();
    }
}

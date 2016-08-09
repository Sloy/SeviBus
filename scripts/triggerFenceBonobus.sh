#!/bin/bash
adb -d shell am broadcast -a "com.sloy.sevibus.action.TRIGGER_FENCE_BONOBUS" --ez "ignoreConditions" "true"

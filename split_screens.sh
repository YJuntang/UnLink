#!/bin/bash
imports=$(cat /tmp/imports.txt)
pkg="package com.juntang2.unlink.ui"

mkdir -p app/src/main/java/com/juntang2/unlink/ui/main
mkdir -p app/src/main/java/com/juntang2/unlink/ui/bulk
mkdir -p app/src/main/java/com/juntang2/unlink/ui/history
mkdir -p app/src/main/java/com/juntang2/unlink/ui/settings

awk -v imports="$imports" -v pkg="package com.juntang2.unlink.ui.main" '
/^@Composable/ && /fun MainScreen/ { flag=1; print pkg "\n" imports "\n" $0; next }
/^@Composable/ && /fun BulkScreen/ { flag=0 }
flag { print }
' app/src/main/java/com/juntang2/unlink/MainActivity.kt > app/src/main/java/com/juntang2/unlink/ui/main/MainScreen.kt

awk -v imports="$imports" -v pkg="package com.juntang2.unlink.ui.bulk" '
/^@Composable/ && /fun BulkScreen/ { flag=1; print pkg "\n" imports "\n" $0; next }
/^@OptIn/ && /fun HistoryScreen/ { flag=0; next }
/^@Composable/ && /fun HistoryScreen/ { flag=0; next }
flag { print }
' app/src/main/java/com/juntang2/unlink/MainActivity.kt > app/src/main/java/com/juntang2/unlink/ui/bulk/BulkScreen.kt

awk -v imports="$imports" -v pkg="package com.juntang2.unlink.ui.history" '
/^@OptIn\(ExperimentalFoundationApi::class\)/ { if (history_next) { flag=1; print pkg "\n" imports "\n" $0; next } else { history_next=1 } }
/^@Composable/ && /fun HistoryScreen/ { if (!flag) { flag=1; print pkg "\n" imports "\n" $0; next } }
/^@OptIn\(ExperimentalMaterial3Api::class\)/ { flag=0 }
/^@Composable/ && /fun SettingsScreen/ { flag=0 }
flag { print }
' app/src/main/java/com/juntang2/unlink/MainActivity.kt > app/src/main/java/com/juntang2/unlink/ui/history/HistoryScreen.kt

awk -v imports="$imports" -v pkg="package com.juntang2.unlink.ui.settings" '
/^@OptIn\(ExperimentalMaterial3Api::class\)/ { if (settings_next) { flag=1; print pkg "\n" imports "\n" $0; next } else { settings_next=1 } }
/^@Composable/ && /fun SettingsScreen/ { if (!flag) { flag=1; print pkg "\n" imports "\n" $0; next } }
flag { print }
' app/src/main/java/com/juntang2/unlink/MainActivity.kt > app/src/main/java/com/juntang2/unlink/ui/settings/SettingsScreen.kt

# Strip screens from MainActivity.kt
awk '
/^@Composable/ && /fun MainScreen/ { skip=1 }
skip==0 { print }
' app/src/main/java/com/juntang2/unlink/MainActivity.kt > app/src/main/java/com/juntang2/unlink/MainActivity_new.kt
mv app/src/main/java/com/juntang2/unlink/MainActivity_new.kt app/src/main/java/com/juntang2/unlink/MainActivity.kt


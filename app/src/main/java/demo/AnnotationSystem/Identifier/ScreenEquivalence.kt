package demo.AnnotationSystem.Identifier

import java.util.*
import kotlin.collections.ArrayList
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import demo.AnnotationSystem.Utilities.*
import kotlin.collections.HashSet

fun isVisible(node: Element): Boolean {
    if (node.boundsInScreen.left >= node.boundsInScreen.right || node.boundsInScreen.top >= node.boundsInScreen.bottom) {
        return false
    }

    if (node.boundsInScreen.right > gScreenW || node.boundsInScreen.bottom > gScreenH) {
        return false
    }

    return true
}

fun isSameArrayList(list0: ArrayList<String>, list1: ArrayList<String>): Boolean {
    //Log.i("DemoLog", "list0: " + ArrayList(HashSet(list0)).sorted().toString())
    //Log.i("DemoLog", "list1: " + ArrayList(HashSet(list1)).sorted().toString())
    //return list0.sorted().toString() == list1.sorted().toString()
    return ArrayList(HashSet(list0)).sorted().toString() == ArrayList(HashSet(list1)).sorted().toString()
}

fun hasDifferentImportantClassNames(screen0: Screen, screen1: Screen): Boolean {
    val importantClassNames = arrayListOf<String>(
            "view.ViewPager",
            "widget.DrawerLayout",
            "widget.HorizontalScrollView",
            "widget.ImageView"
    )

    val set0 = HashSet<String>()
    val set1 = HashSet<String>()

    for (node in screen0.elementList) {
        for (className in importantClassNames) {
            if (node.className.contains(className)) {
                set0.add(className)
            }
        }

    }

    for (node in screen1.elementList) {
        for (className in importantClassNames) {
            if (node.className.contains(className)) {
                set1.add(className)
            }
        }
    }

    return !set0.equals(set1)
}

fun getSimilarityOfClassNames(screen0: Screen, screen1: Screen): Double {
    // TODO: Ignore Google ADs, etc
    val set0 = HashSet<String>()
    val set1 = HashSet<String>()

    for (node in screen0.elementList) {
        // TODO: && node.className.contains(".")
        if (isVisible(node)) {
            set0.add(node.className)
        }
    }
    for (node in screen1.elementList) {
        if (isVisible(node)) {
            set1.add(node.className)
        }
    }

    if (screen0.id == "1521757308759" && screen1.id == "1521757369793") {
        Log.i("DemoLog", set0.toString())
        Log.i("DemoLog", set1.toString())
    }

    return getSetSimilarity(set0, set1)
}

fun getBoolean(accessibilityNodeInfoToString: String?, propertyName: String): Boolean {
    if (accessibilityNodeInfoToString == null) { return false }

    val startIndex = accessibilityNodeInfoToString.indexOf(propertyName) + propertyName.length + 2
    return accessibilityNodeInfoToString[startIndex] == 't'
    //"android.view.accessibility.AccessibilityNodeInfo@8023a058; boundsInParent: Rect(0, 0 - 1216, 123); boundsInScreen: Rect(1552, 1592 - 1440, 1715); packageName: com.jobrapp.jobr; className: android.widget.TextView; text: Applications that fail to submit, appear here. Open them to resolve issues and reapply.; error: null; maxTextLength: -1; contentDescription: null; viewIdResourceName: com.jobrapp.jobr:id/empty_text; checkable: false; checked: false; focusable: false; focused: false; selected: false; clickable: false; longClickable: false; contextClickable: false; enabled: true; password: false; scrollable: false; actions: [AccessibilityAction: ACTION_SELECT - null, AccessibilityAction: ACTION_CLEAR_SELECTION - null, AccessibilityAction: ACTION_ACCESSIBILITY_FOCUS - null, AccessibilityAction: ACTION_NEXT_AT_MOVEMENT_GRANULARITY - null, AccessibilityAction: ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY - null, AccessibilityAction: ACTION_SET_SELECTION - null, AccessibilityAction: ACTION_SHOW_ON_SCREEN - null]"
}

fun checkedRadioIndex(screen: Screen): Int {
    for (node in screen.elementList) {
        if (node.className.contains("android.widget.RadioGroup")) {
            var radioGroupNode = node
            for (i in 0..radioGroupNode.childrenIndexList.size - 1) {
                if (radioGroupNode.childrenIndexList[i] != -1) {
                    val element = screen.elementList[radioGroupNode.childrenIndexList[i]]
                    if (element.accessibilityProperties.isChecked) {
                        return i
                    }
                }
            }
        }
    }
    return -1
    //android.widget.RadioGroup
}

fun hasDifferentCheckedRadio(screen0: Screen, screen1: Screen): Boolean {
    val checkedRadioIndex0 = checkedRadioIndex(screen0)
    val checkedRadioIndex1 = checkedRadioIndex(screen1)
    return checkedRadioIndex0 != checkedRadioIndex1
}

fun selectedTabIndex(screen: Screen): Int {
    // Does any TabLayout have no HorizontalScrollView? Is view.ViewPager the key?
    for (node in screen.elementList) {
        if (node.className.contains("android.widget.HorizontalScrollView")) {
            var inInfiniteLoop = false
            var tabNode = node
            while (tabNode.childrenIndexList.size == 1 && !inInfiniteLoop) {
                if (tabNode.childrenIndexList[0] != -1) {
                    tabNode = screen.elementList[tabNode.childrenIndexList[0]]
                } else {
                    inInfiniteLoop = true
                }
            }
            for (i in 0..tabNode.childrenIndexList.size - 1) {
                if (tabNode.childrenIndexList[i] != -1) {
                    val element = screen.elementList[tabNode.childrenIndexList[i]]
                    if (element.accessibilityProperties.isSelected) {
                        return i
                    }
                }
            }
        }
    }
    // No TabLayout
    return -1
}

fun hasDifferentSelectedTab(screen0: Screen, screen1: Screen): Boolean {
    // Or we should compare selected/unselected structure?
    val selectedTabIndex0 = selectedTabIndex(screen0)
    val selectedTabIndex1 = selectedTabIndex(screen1)
    //Log.i("DemoLog", "Tab:" + selectedTabIndex0 + "," + selectedTabIndex1)
    return selectedTabIndex0 != selectedTabIndex1
}

fun hasDifferentTalkbackNodes(screen0: Screen, screen1: Screen, onlyCompareSet: Boolean): Boolean {
    val talkbackNodeIdList0 = screen0.talkbackElementIdList
    val talkbackNodeIdList1 = screen1.talkbackElementIdList

    if (!onlyCompareSet && talkbackNodeIdList0.size != talkbackNodeIdList1.size) {
        return true
    } else {
        val talkbackNodeClassNameList0 = ArrayList<String>()
        val talkbackNodeClassNameList1 = ArrayList<String>()
        for (nodeId in talkbackNodeIdList0) {
            val node = screen0.getElement(nodeId)
            // TODO: && node.className.contains(".")
            if (node != null && isVisible(node)) {
                talkbackNodeClassNameList0.add(node.className)
            }
        }
        for (nodeId in talkbackNodeIdList1) {
            val node = screen1.getElement(nodeId)
            if (node != null && isVisible(node)) {
                talkbackNodeClassNameList1.add(node.className)
            }
        }
        Collections.sort(talkbackNodeClassNameList0)
        Collections.sort(talkbackNodeClassNameList1)
        //Log.i("DemoLog", "Talkback0: " + talkbackNodeClassNameList0.toString())
        //Log.i("DemoLog", "Talkback1: " + talkbackNodeClassNameList1.toString())

        if (onlyCompareSet) {
            return ArrayList(HashSet(talkbackNodeClassNameList0)).sorted().toString() != ArrayList(HashSet(talkbackNodeClassNameList1)).sorted().toString()
        } else {
            return talkbackNodeClassNameList0.toString() != talkbackNodeClassNameList1.toString()
        }
    }
}



fun getAllViewIdUnderNode(node: Element, screen: Screen): ArrayList<String> {
    val viewIdResourceNameList = ArrayList<String>()
    getAllViewIdHelper(viewIdResourceNameList, node, screen)
    return viewIdResourceNameList
}

fun getAllViewIdHelper(viewIdResourceNameList: ArrayList<String>,
                       node: Element?,
                       screen: Screen) {
    if (node == null) return
    if (node.viewIdResourceName != null) {
        viewIdResourceNameList.add(node.viewIdResourceName!!)
    }
    for (childIndex in node.childrenIndexList) {
        if (childIndex >=0) {
            getAllViewIdHelper(viewIdResourceNameList, screen.elementList[childIndex], screen)
        }
    }
}

fun getViewIdResourceNameListUnderNode(node: Element, screen: Screen): ArrayList<String> {
    val viewIdResourceNameList = ArrayList<String>()
    getViewIdResourceNameListHelper(viewIdResourceNameList, node, screen)
    return viewIdResourceNameList
}

/*
fun getViewIdResourceNameListHelper(viewIdResourceNameList: ArrayList<String>,
                                    node: Element?,
                                    screen: Screen) {
    if (node == null) return
    if (node.viewIdResourceName != null && isVisible(node)) {
        if (node.viewIdResourceName!!.contains(".")) {
            viewIdResourceNameList.add(node.viewIdResourceName!!)
        }
    }
    for (childIndex in node.childrenIndexList) {
        if (childIndex >= 0) {
            getViewIdResourceNameListHelper(viewIdResourceNameList, screen.elementList[childIndex], screen)
        }
    }
}
*/

fun getViewIdResourceNameListHelper(viewIdResourceNameList: ArrayList<String>,
                               node: Element?,
                               screen: Screen) {
    if (node == null) return
    if (node.viewIdResourceName != null && isVisible(node)) {
        viewIdResourceNameList.add(node.viewIdResourceName!!)
    }
    for (childIndex in node.childrenIndexList) {
        if (childIndex >=0 ) {
            getViewIdResourceNameListHelper(viewIdResourceNameList, screen.elementList[childIndex], screen)
        }
    }
}

fun findDialogNode(screen: Screen): Element? {
    if (screen.elementList.size == 0) {
        return null
    }
    // Only need to check root node, which is smaller than screen
    // E.g. OfferUp->Invite A Friend, or Message->Advance Setting->Phone#
    // Shouldn't check height, as the threeKeyBar may or may not take height in root view...
    if (screen.elementList[0].boundsInScreen.width() != 1440) {
        return screen.elementList[0]
    }

    return null
}

fun checkDialog(screen0: Screen, screen1: Screen): String {
    val dialogNode0 = findDialogNode(screen0)
    val dialogNode1 = findDialogNode(screen1)
    if (dialogNode0 == null && dialogNode1 == null) {
        return "NoDialog"
    } else if (dialogNode0 == null || dialogNode1 == null) {
        return "OneDialog"
    }

    if (getSimilarityOfClassNames(screen0, screen1) != 1.0 || getViewIdResourceNameListSimilarity(screen0, screen1) != 1.0) {
        return "DifferentDialog"
    }

    return "SameDialog"
}


fun findDrawerLayoutNode(screen: Screen): Element? {
    //android.support.v4.widget.DrawerLayout
    //Only compare nodes under DrawerLayout. Ignore others in background
    for (node in screen.elementList) {
        if (node.className.contains("widget.DrawerLayout")) {
            //Log.i("DemoLog", "Screen: " + screen.id + ", Drawer: " + node.id)
            if (node.childrenIndexList.size > 1) {
                var sideDrawerNodeId = node.childrenIndexList[1]
                val node0 = screen.elementList[node.childrenIndexList[0]]
                val node1 = screen.elementList[node.childrenIndexList[1]]
                if (node0.boundsInScreen.width() < node1.boundsInScreen.width()) {
                    sideDrawerNodeId = node.childrenIndexList[0]
                }
                return screen.elementList[sideDrawerNodeId]
            }
        }
    }
    return null
}

fun checkDrawerLayout(screen0: Screen, screen1: Screen): String {
    val drawerNode0 = findDrawerLayoutNode(screen0)
    val drawerNode1 = findDrawerLayoutNode(screen1)
    if (drawerNode0 == null && drawerNode1 == null) {
        return "NoDrawer"
    } else if (drawerNode0 == null || drawerNode1 == null) {
        return "OneDrawer"
    }

    val viewIdResourceNameList0 = getViewIdResourceNameListUnderNode(drawerNode0, screen0)
    val viewIdResourceNameList1 = getViewIdResourceNameListUnderNode(drawerNode1, screen1)

    //Log.i("DemoLog", "Drawer screen0: " + screen0.id + ", screen1: " + screen1.id)
    if (isSameArrayList(viewIdResourceNameList0, viewIdResourceNameList1)) {
        return "SameDrawer"
    }
    return "DifferentDrawer"
}

fun removeADViewId(set: Set<String>): Set<String> {
    val resultSet = HashSet<String>()
    for (viewId in set) {
        if (viewId.contains(".") || viewId.contains("/")) {
            resultSet.add(viewId)
        }
    }
    return set
}

fun getViewIdResourceNameListSimilarity(screen0: Screen, screen1: Screen): Double {
    // Check viewIdResourceName list
    // A high similarity guarantees two screens are same
    // Otherwise we need to check structure
    val set0 = removeADViewId(HashSet(screen0.viewIdResourceNameList))
    val set1 = removeADViewId(HashSet(screen1.viewIdResourceNameList))

    //Log.i("DemoLog", "ViewId0: " + set0.toString())
    //Log.i("DemoLog", "ViewId1: " + set1.toString())

    return getSetSimilarity(set0, set1)
}

fun hasDifferentNodeDepth(screen0: Screen, screen1: Screen): Boolean {
    var maxDepth0 = 0
    var maxDepth1 = 0
    for (node in screen0.elementList) {
        if (node.depth > maxDepth0) {
            maxDepth0 = node.depth
        }
    }
    for (node in screen1.elementList) {
        if (node.depth > maxDepth1) {
            maxDepth1 = node.depth
        }
    }
    return maxDepth0 != maxDepth1
}


fun isSameScreen(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    if (showDebug) {
        Log.i("DemoLog", "screen0: "+ screen0.id + ", screen1: " + screen1.id)
        Log.i("DemoLog", "similarity: " + getSetSimilarity(HashSet(screen0.viewIdResourceNameList), HashSet(screen1.viewIdResourceNameList)))
        Log.i("DemoLog", "checkDrawerLayout: " + checkDrawerLayout(screen0, screen1))
        Log.i("DemoLog", "checkDialog: " + checkDialog(screen0, screen1))
        Log.i("DemoLog", "hasDifferentSelectedTab: " + hasDifferentSelectedTab(screen0, screen1))
        Log.i("DemoLog", "getSimilarityOfClassNames: " + getSimilarityOfClassNames(screen0, screen1))
        Log.i("DemoLog", "hasDifferentTalkbackNodes: " + hasDifferentTalkbackNodes(screen0, screen1, false))
        Log.i("DemoLog", "hasDifferentTalkbackNodes(onlyCompareSet): " + hasDifferentTalkbackNodes(screen0, screen1, true))
        Log.i("DemoLog", "hasDifferentNodeDepth: " + hasDifferentNodeDepth(screen0, screen1))
    }

    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 3. Check Dialog
    when (checkDialog(screen0, screen1)) {
        "OneDialog" -> return false
        "SameDialog" -> return true
        "DifferentDialog" -> return false
    }

    // 4. Check Tab Layout
    if (hasDifferentSelectedTab(screen0, screen1)) {
        return false
    }

    // 5. Check Radio Button Group
    if (hasDifferentCheckedRadio(screen0, screen1)) {
        return false
    }

    // 7. Compare visible classnames, remove ADs (code changed in getSimilarityOfClassNames)
    if (getSimilarityOfClassNames(screen0, screen1) != 1.0) {
        return false
    }

    // 6. Compare visible viewId
    val set0 = HashSet(screen0.viewIdResourceNameList)
    val set1 = HashSet(screen1.viewIdResourceNameList)

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }


    return false
}


fun isSameScreenJson(screen: Screen, heuristics: JsonObject, showDebug: Boolean = false): Boolean {

    if (selectedTabIndex(screen) != heuristics.getAsJsonPrimitive("tabIndex").asInt) {
        return false
    }

    if (checkedRadioIndex(screen) != heuristics.getAsJsonPrimitive("radioIndex").asInt) {
        return false
    }

    val set0 = HashSet<String>()
    val set1 = HashSet<String>()
    for (node in screen.elementList) {
        if (isVisible(node)) {
            set0.add(node.className)
        }
    }
    if (set0.contains("null")) {
        set0.remove("null")
    }
    for (cnObj in heuristics.getAsJsonArray("classNames")) {
        set1.add(cnObj.asString)
    }
    if (showDebug) {
        Log.i("DemoLog", "classNames similarity: " + getSetSimilarity(set0, set1))
        Log.i("DemoLog", "classNames0: " + set0)
        Log.i("DemoLog", "classNames1: " + set1)
    }
    if (getSetSimilarity(set0, set1) != 1.0) {
        return false
    }

    val set2 = HashSet(screen.viewIdResourceNameList)
    if (set2.contains(null)) {
        set2.remove(null)
        set2.add("")
    }
    val set3 = HashSet<String>()
    for (viObj in heuristics.getAsJsonArray("resourceIds")) {
        set3.add(viObj.asString)
    }
    if (showDebug) {
        Log.i("DemoLog", "resIds null \"\": " + set2.contains(null) + set2.contains(""))
        Log.i("DemoLog", "resIds similarity: " + getSetSimilarity(set2, set3))
    }
    if (getSetSimilarity(set2, set3) == 1.0) {
        return true
    }

    return false
}
















































































































































fun isSameScreen0(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    val set0 = HashSet(getAllViewIdUnderNode(screen0.elementList[0], screen0))
    val set1 = HashSet(getAllViewIdUnderNode(screen1.elementList[0], screen1))

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }

    return false
}

fun isSameScreen1(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 0. Compare All viewId
    val set0 = HashSet(getAllViewIdUnderNode(screen0.elementList[0], screen0))
    val set1 = HashSet(getAllViewIdUnderNode(screen1.elementList[0], screen1))

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }

    return false
}

fun isSameScreen2(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 0. Compare All viewId
    val set0 = HashSet(getAllViewIdUnderNode(screen0.elementList[0], screen0))
    val set1 = HashSet(getAllViewIdUnderNode(screen1.elementList[0], screen1))

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }

    return false
}

fun isSameScreen3(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 3. Check Dialog
    when (checkDialog(screen0, screen1)) {
        "OneDialog" -> return false
        "SameDialog" -> return true
        "DifferentDialog" -> return false
    }

    // 0. Compare All viewId
    val set0 = HashSet(getAllViewIdUnderNode(screen0.elementList[0], screen0))
    val set1 = HashSet(getAllViewIdUnderNode(screen1.elementList[0], screen1))

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }

    return false
}


fun isSameScreen4(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 3. Check Dialog
    when (checkDialog(screen0, screen1)) {
        "OneDialog" -> return false
        "SameDialog" -> return true
        "DifferentDialog" -> return false
    }

    // 4. Check Tab Layout
    if (hasDifferentSelectedTab(screen0, screen1)) {
        return false
    }

    // 0. Compare All viewId
    val set0 = HashSet(getAllViewIdUnderNode(screen0.elementList[0], screen0))
    val set1 = HashSet(getAllViewIdUnderNode(screen1.elementList[0], screen1))

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }

    return false
}



fun isSameScreen5(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    if (showDebug) {
        Log.i("DemoLog", "screen0: "+ screen0.id + ", screen1: " + screen1.id)
        Log.i("DemoLog", "similarity: " + getSetSimilarity(HashSet(screen0.viewIdResourceNameList), HashSet(screen1.viewIdResourceNameList)))
        Log.i("DemoLog", "checkDrawerLayout: " + checkDrawerLayout(screen0, screen1))
        Log.i("DemoLog", "checkDialog: " + checkDialog(screen0, screen1))
        Log.i("DemoLog", "hasDifferentSelectedTab: " + hasDifferentSelectedTab(screen0, screen1))
        Log.i("DemoLog", "getSimilarityOfClassNames: " + getSimilarityOfClassNames(screen0, screen1))
        Log.i("DemoLog", "hasDifferentTalkbackNodes: " + hasDifferentTalkbackNodes(screen0, screen1, false))
        Log.i("DemoLog", "hasDifferentTalkbackNodes(onlyCompareSet): " + hasDifferentTalkbackNodes(screen0, screen1, true))
        Log.i("DemoLog", "hasDifferentNodeDepth: " + hasDifferentNodeDepth(screen0, screen1))
    }

    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 3. Check Dialog
    when (checkDialog(screen0, screen1)) {
        "OneDialog" -> return false
        "SameDialog" -> return true
        "DifferentDialog" -> return false
    }

    // 4. Check Tab Layout
    if (hasDifferentSelectedTab(screen0, screen1)) {
        return false
    }

    // 5. Check Radio Button Group
    if (hasDifferentCheckedRadio(screen0, screen1)) {
        return false
    }

    // 0. Compare All viewId
    val set0 = HashSet(getAllViewIdUnderNode(screen0.elementList[0], screen0))
    val set1 = HashSet(getAllViewIdUnderNode(screen1.elementList[0], screen1))

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }

    return false
}

fun isSameScreen6(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    if (showDebug) {
        Log.i("DemoLog", "screen0: "+ screen0.id + ", screen1: " + screen1.id)
        Log.i("DemoLog", "similarity: " + getSetSimilarity(HashSet(screen0.viewIdResourceNameList), HashSet(screen1.viewIdResourceNameList)))
        Log.i("DemoLog", "checkDrawerLayout: " + checkDrawerLayout(screen0, screen1))
        Log.i("DemoLog", "checkDialog: " + checkDialog(screen0, screen1))
        Log.i("DemoLog", "hasDifferentSelectedTab: " + hasDifferentSelectedTab(screen0, screen1))
        Log.i("DemoLog", "getSimilarityOfClassNames: " + getSimilarityOfClassNames(screen0, screen1))
        Log.i("DemoLog", "hasDifferentTalkbackNodes: " + hasDifferentTalkbackNodes(screen0, screen1, false))
        Log.i("DemoLog", "hasDifferentTalkbackNodes(onlyCompareSet): " + hasDifferentTalkbackNodes(screen0, screen1, true))
        Log.i("DemoLog", "hasDifferentNodeDepth: " + hasDifferentNodeDepth(screen0, screen1))
    }

    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 3. Check Dialog
    when (checkDialog(screen0, screen1)) {
        "OneDialog" -> return false
        "SameDialog" -> return true
        "DifferentDialog" -> return false
    }

    // 4. Check Tab Layout
    if (hasDifferentSelectedTab(screen0, screen1)) {
        return false
    }

    // 5. Check Radio Button Group
    if (hasDifferentCheckedRadio(screen0, screen1)) {
        return false
    }

    // 6. Compare visible viewId
    val set0 = HashSet(screen0.viewIdResourceNameList)
    val set1 = HashSet(screen1.viewIdResourceNameList)

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }


    return false
}

fun isSameScreen7(screen0: Screen, screen1: Screen, showDebug: Boolean = false): Boolean {
    if (showDebug) {
        Log.i("DemoLog", "screen0: "+ screen0.id + ", screen1: " + screen1.id)
        Log.i("DemoLog", "similarity: " + getSetSimilarity(HashSet(screen0.viewIdResourceNameList), HashSet(screen1.viewIdResourceNameList)))
        Log.i("DemoLog", "checkDrawerLayout: " + checkDrawerLayout(screen0, screen1))
        Log.i("DemoLog", "checkDialog: " + checkDialog(screen0, screen1))
        Log.i("DemoLog", "hasDifferentSelectedTab: " + hasDifferentSelectedTab(screen0, screen1))
        Log.i("DemoLog", "getSimilarityOfClassNames: " + getSimilarityOfClassNames(screen0, screen1))
        Log.i("DemoLog", "hasDifferentTalkbackNodes: " + hasDifferentTalkbackNodes(screen0, screen1, false))
        Log.i("DemoLog", "hasDifferentTalkbackNodes(onlyCompareSet): " + hasDifferentTalkbackNodes(screen0, screen1, true))
        Log.i("DemoLog", "hasDifferentNodeDepth: " + hasDifferentNodeDepth(screen0, screen1))
    }

    // 1. Check activity name (caution: activityName may be empty)
    if (screen0.activityName != ""
            && screen1.activityName != ""
            && screen0.activityName != screen1.activityName) {
        return false
    }

    // 2. Check Side Menu
    when (checkDrawerLayout(screen0, screen1)) {
        "OneDrawer" -> return false
        "SameDrawer" -> return true
        "DifferentDrawer" -> return false
    }

    // 3. Check Dialog
    when (checkDialog(screen0, screen1)) {
        "OneDialog" -> return false
        "SameDialog" -> return true
        "DifferentDialog" -> return false
    }

    // 4. Check Tab Layout
    if (hasDifferentSelectedTab(screen0, screen1)) {
        return false
    }

    // 5. Check Radio Button Group
    if (hasDifferentCheckedRadio(screen0, screen1)) {
        return false
    }

    // 7. Compare visible classnames, remove ADs (code changed in getSimilarityOfClassNames)
    if (getSimilarityOfClassNames(screen0, screen1) != 1.0) {
        return false
    }

    // 6. Compare visible viewId
    val set0 = HashSet(screen0.viewIdResourceNameList)
    val set1 = HashSet(screen1.viewIdResourceNameList)

    if (getSetSimilarity(set0, set1) == 1.0) {
        return true
    }


    return false
}
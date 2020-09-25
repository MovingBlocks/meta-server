<!DOCTYPE html>
<html lang="en">

<#include "metainfo.ftl">

  <body>

    <div class="container">

<#assign tab = "about">
<#include "navigation.ftl">

<h3>Services</h3>

This is the Terasology meta service. It provides live information about game servers, available modules, etc.
More precisely, this instance serves the following data sets in JSON format.
<br><br>

<h4><a href="modules/list">/modules/list</a></h4>
<p>Provides all listed module metadata.</p>

<pre>
[
  {
    "id": "myModuleId",
    "version": "0.1.1-SNAPSHOT",
    "displayName": "My Great Module",
    "description": "This is what it's about!",
    "dependencies": [],
    "requiredPermissions": [],
    "lastUpdated": "2015-06-08T05:39:40.811+02:00",
    "downloadUri": "http://artifactory.terasology.org/artifactory/...",
    "artifactSize": 1337
  },
  ...
]
</pre>      
<br>

<h4><a href="modules/list/latest">/modules/list/latest</a></h4>
<p>Provides a list of metadata of the latest module versions. There is only one entry per module.</p>
<br>

<h4><a href="modules/list/myModule">/modules/list/myModule</a></h4>
<p>Provides a list of metadata of a specified module for all versions.</p>
<br>

<h4><a href="modules/list/myModule/latest">/modules/list/myModule/latest</a></h4>
<p>Provides the latest metadata of a specified module version, if available. Status 404 otherwise.</p>
<br>

<h4><a href="modules/list/myModule/myVersion">/modules/list/myModule/myVersion</a></h4>
<p>Provides the metadata of a specified module version, if available. Status 404 otherwise.</p>
<br>


<h4><a href="servers/list">/servers/list</a></h4>
<p>Provides a list of game servers</p>

<pre>
[
  {
    "address": "myserver.org",
    "name": "My Server",
    "owner": "nickname",
    "country": "KY",
    "stateprov": "George Town",
    "city": "Whitehall Estate",
    "port": 25777,
    "active": true
  },
  ...
]
</pre>
<br>


<h4><a href="modules/update-all">/modules/update-all</a> (POST)</h4>
<p>Triggers updating all modules.</p>
<br>


<h4><a href="modules/update">/modules/update</a> (POST)</h4>
<p>Triggers updating a specified module, for example through 
the <a href="https://wiki.jenkins-ci.org/display/JENKINS/Notification+Plugin">Jenkins Notification Plugin</a>.</p>
<pre>
{  
  "name": "myModuleId",
}
</pre>
<br>


<hr>

<h3>About Terasology</h3>

<p>Terasology is a game that pays ample tribute to <a href="http://www.minecraft.net">Minecraft</a> in 
initial look and origin, but stakes out its own niche by aiming for the NPC-helper and caretaker focus 
from such games as <a href="http://www.bay12games.com/dwarves">Dwarf Fortress</a> and 
<a href="http://en.wikipedia.org/wiki/Dungeon_Keeper">Dungeon Keeper</a>, while striving for added depth 
and sophistication.</p>

<p>Terasology is an open source project started by Benjamin "begla" Glatzel to research procedural terrain 
generation and efficient rendering techniques in Java using the <a href="http://lwjgl.org">LWJGL</a>. 
The engine uses a block-based voxel-like approach as seen in Minecraft. You can check out his blog at 
<a href="http://blog.movingblocks.net">Moving Blocks!</a></p>

<p>The creators of Terasology are a diverse mix of software developers, game testers, graphic artists, 
and musicians. Get involved by checking out our <a href="http://forum.terasology.org/index.php">Community Portal</a>,
 <a href="http://www.facebook.com/pages/Terasology/248329655219905">Facebook Page</a>, 
 <a href="https://twitter.com/Terasology">Twitter</a>, <a href="https://plus.google.com/b/103835217961917018533">G+</a>, 
 or <a href="http://www.reddit.com/r/Terasology">Reddit</a></p>

<p>Terasology is licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0.html">Apache 2.0 License</a> 
and available in source code form at <a href="https://github.com/MovingBlocks/Terasology">GitHub</a>.</p>

<#include "footer.ftl">

    </div> <!-- /container -->

</body></html>

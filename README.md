# Enguage - Unifying Speech and Computation
<p>Enguage is a natural language engine: a virtual machine for voice.
It uses human social interaction as a model for human computer interaction, 
by using vocal norms to map between any utterance and its reply. 
This is different to the simple imperative model used by smart speaker 
devices ('play this', 'buy that') because the vocal norms are determined 
by voice. 
This means machine instruction is also generated by voice — 
you directly interact with the machine: 
there’s no pesky JavaScript or corporate websites to get in the way!</p>

[Installation](doc/install.md)<br/>

## Containers

[docker Webserver](doc/container.md)<br/>
[Running jarfile](doc/jarfile.md)<br/>
[Web Server](doc/httpd.md)<br/>
Containerised (flatpak) app - WIP<br/>
This requires:
<ul>
<li> apt install flatpak
<li> apt install flatpak-builder
<li> flatpak install flathub org.freedesktop.Platform//19.08 org.freedesktop.Sdk//19.08
<li> flatpak install flathub org.freedesktop.Sdk.Extension.openjdk11
<li> flatpak-builder --user --install --force-clean inst org.enguage.Eng.yaml
<li> flatpak run --versbose org.enguage.Eng hello
</ul>

## Examples

[Programming](doc/programming.md)<br/>
[Examples](doc/examples.md)<br/>
[MySql Example](doc/mySql.md)

<h3>Play with Enguage:</h3>
<P>For further examples of repertoires, see the etc/ directory.
The most complete is need+needs.txt, but other examples include
meeting.txt which is both a temporal and a spatial concept.
The output from the test you’ve already run is produced from the
repertoires in the assets directory—search through this for examples o
f how you can say/code things. If a variable has PHRASE- in front of it
it will match more than one word. If it has NUMERIC- it will match an
apparent number (this, I’m afraid is English based for the moment,
e.g. ‘a’ = 1, sorry!)
<p>If you don’t like it, tell me; if you, do tell others!
<p>Happy talking!<br/>
martin@wheatman.net

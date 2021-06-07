import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
    id("myaa.subkt")
}

fun String.isKaraTemplate(): Boolean {
	return this.startsWith("code") || this.startsWith("template") || this.startsWith("mixin")
}

fun EventLine.isKaraTemplate(): Boolean {
	return this.comment && this.effect.isKaraTemplate()
}


subs {
    readProperties("sub.properties")
    episodes(getList("episodes"))


	val op_ktemplate by task<Automation> {
        if (propertyExists("OP")) {
            from(get("OP"))
        }

        video(get("premux"))
		script("0x.KaraTemplater.moon")
		macro("0x539's Templater")
		loglevel(Automation.LogLevel.WARNING)
	}


    val ed_ktemplate by task<Automation> {
            if (propertyExists("ED")) {
                from(get("ED"))
            }

            video(get("premux"))
            script("0x.KaraTemplater.moon")
            macro("0x539's Templater")
            loglevel(Automation.LogLevel.WARNING)
        }

    merge {
        from(get("dialogue"))

        if (propertyExists("OP")) {
            from(op_ktemplate.item()) {
                syncTargetTime(getAs<Duration>("opsync"))
            }
        }

        if (propertyExists("ED")) {
            from(ed_ktemplate.item()) {
                syncTargetTime(getAs<Duration>("edsync"))
            }
        }

        if (file(get("INS")).exists()) {
            from(getList("INS"))
        }

        from(getList("TS"))
    }

	val cleanmerge by task<ASS> {
		from(merge.item())
    	ass {
			events.lines.removeIf { it.isKaraTemplate() }
	    }

        out(get("mergedname"))
	}

    chapters {
        from(merge.item())
        chapterMarker("chapter")
    }


    mux {
        title(get("muxtitle"))

		from(get("premux")) {
			video {
				lang("jpn")
				default(true)
			}
			audio {
				lang("jpn")
				default(true)
			}
            includeChapters(false)
			attachments { include(false) }
		}

		from(cleanmerge.item()) {
			tracks {
				lang("eng")
                name(get("group"))
				default(true)
				forced(true)
				compression(CompressionType.ZLIB)
			}
		}

        chapters(chapters.item()) { lang("eng") }

        attach(get("common_fonts")) {
            includeExtensions("ttf", "otf")
        }

        attach(get("fonts")) {
            includeExtensions("ttf", "otf")
        }

        if (propertyExists("OP")) {
            attach(get("opfonts")) {
                includeExtensions("ttf", "otf")
            }
        }

        if (propertyExists("ED")) {
            attach(get("edfonts")) {
                includeExtensions("ttf", "otf")
            }
        }

        out(get("muxout"))
    }
}

FROM selenoid/vnc:chrome_93.0
USER root
RUN apt-get update && apt-get install -y openjdk-11-jdk maven
RUN printf "55,60d54\n< /usr/bin/fileserver &\n< FILESERVER_PID=\$!\n< \n< /usr/bin/devtools &\n< DEVTOOLS_PID=\$!\n< \n90,93c90\n< DISPLAY=\"\$DISPLAY\" /usr/bin/chromedriver --port=4444 --allowed-ips='' \${DRIVER_ARGS} &\n< DRIVER_PID=\$!\n< \n< wait\n---\n> cd /usr/local/310-project && xterm\n" | patch entrypoint.sh
RUN echo '/opt/google/chrome/chrome --no-sandbox --disable-gpu --allow-pre-commit-input --disable-background-networking --disable-client-side-phishing-detection --disable-default-apps --disable-hang-monitor --disable-popup-blocking --disable-prompt-on-repost --disable-sync --enable-blink-features=ShadowDOMV0 --enable-logging --log-level=0 --no-first-run --no-service-autorun --password-store=basic --remote-debugging-port=0 --test-type=webdriver --use-mock-keychain --user-data-dir="$(mktemp -d)" --enable-crashpad "$*"' > /usr/local/bin/chrome-browser && chmod 755 /usr/local/bin/chrome-browser
VOLUME ["/root/.m2", "/usr/local/310-project"]

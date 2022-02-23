# React Widgets

## Development Environment

1. Install [Node.js](https://nodejs.org/en/download/)
2. Install [yarn](https://yarnpkg.com/getting-started/install)
3. Run `yarn install` in this directory

## Development

1. Play with the code in `src`, use `src/index.html` to design your widget
2. Run `yarn start` to start the dev server (`http://localhost:1234`)
3. When you are done run `yarn build` to build (all scripts will be bundled into
    a single file and placed in `src/main/webapp/react-widget-static`)
4. Add a `<div>` (other tags will also work) with the correct id to the jsp
    page, and then add a `<script>` tag to [load the bundled script](https://github.com/CSCI310/20213-project-team29/blob/0e893d39cf8b6ebb6f377d84dc2f328607166fb8/src/main/webapp/index.jsp#L22).

## Accessing TM API

I created a servlet at `/api/tm-api-proxy` to proxy requests to the TM API.
We need to do this because of the same origin policy. You make calls to
the proxy the same way as the TM API, but you don't need to include the
key (it is automatically added to request by the servlet). You need to log
into the app (use `/signup` for now) to use the proxy. The dev server is
set up such that requests to `/signup` and `/api/*` will be forwarded to
jetty running at `localhost:8080`.

## Helpful Links

- [Getting Started with React.js](https://reactjs.org/docs/getting-started.html)
- [Semantic UI React Documentation](https://react.semantic-ui.com/)

// {project}/webpack.config.d/sqljs-config.js
const path = require("path");

const sqlPersistWorkerPath = path.resolve(__dirname, "../../../../webApp/sqljs-persist.worker.js");

config.resolve = config.resolve || {};
config.resolve.fallback = {
  ...(config.resolve.fallback || {}),
  fs: false,
  path: false,
  crypto: false,
};
config.resolve.alias = {
  ...(config.resolve.alias || {}),
  "@cashapp/sqldelight-sqljs-worker/sqljs.worker.js": sqlPersistWorkerPath,
  "sql.js$": path.resolve(__dirname, "../../node_modules/sql.js/dist/sql-wasm.js"),
};

if (!global.__sqljsConfigLoaded) {
  global.__sqljsConfigLoaded = true;
  const CopyWebpackPlugin = require("copy-webpack-plugin");
  config.plugins = config.plugins || [];
  config.plugins.push(
    new CopyWebpackPlugin({
      patterns: [
        {
          from: "../../node_modules/sql.js/dist/sql-wasm.wasm",
          to: "sql-wasm.wasm",
        },
      ],
    }),
  );
}

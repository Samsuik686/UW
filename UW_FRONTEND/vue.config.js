const webpack = require('webpack');
module.exports = {
    publicPath: '/uw_system',
    productionSourceMap: false,
    devServer: {
        open: true,
        host: '0.0.0.0',
        port: 10086,
        proxy: {
            '/api': {
                target: '<url>',
                ws: true,
                changOrigin: true
            }
        }
    },
    configureWebpack: {
        plugins: [
            new webpack.ProvidePlugin({
                'window.Quill': 'quill/dist/quill.js',
                'Quill': 'quill/dist/quill.js'
            }),
        ]
    }
};
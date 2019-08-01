module.exports = {
    publicPath: '/uw_system',
    devServer: {
        open: true,
        host: '0.0.0.0',
        port: 1015,
        proxy: {
            '/api': {
                target: '<url>',
                ws: true,
                changOrigin: true
            }
        }
    },
};
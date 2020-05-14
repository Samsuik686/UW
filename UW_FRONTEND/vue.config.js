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
        },
/*        before(app) {
            const bodyParser = require('body-parser');
            app.use(bodyParser.json());
            app.post('/manage/company/getCompanies', (req, res) => {
                res.json({
                    "result": 200,
                    "data": [
                        {"id": 1, "name": "几米智造", "nickname": "几米智造", "companyCode": "JIMI"},
                        {"id": 2, "name": "黑石科技", "nickname": "黑石科技", "companyCode": "HEISHI"},
                    ]
                })
            });
            app.post('/manage/company/add', (req, res) => {
                res.json({
                    "result": 200,
                    "data": "success"
                })
            });
            app.post('/manage/company/update', (req, res) => {
                res.json({
                    "result": 200,
                    "data": "success"
                })
            });
            app.post('/manage/company/delete', (req, res) => {
                res.json({
                    "result": 200,
                    "data": "success"
                })
            });
        }*/
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
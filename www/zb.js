var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {exec(success, error, 'zb', 'coolMethod' , [arg0]);};
exports.init       = function (arg0, success, error) {exec(success, error, 'zb', 'init'       , [arg0]);};
exports.connect    = function (arg0, success, error) {exec(success, error, 'zb', 'connect'    , [arg0]);};
exports.listen     = function (arg0, success, error) {exec(success, error, 'zb', 'listen'     , [arg0]);};
exports.inventory  = function (arg0, success, error) {exec(success, error, 'zb', 'inventory'  , [arg0]);};
exports.beep       = function (arg0, success, error) {exec(success, error, 'zb', 'beep'       , [arg0]);};
exports.battery    = function (arg0, success, error) {exec(success, error, 'zb', 'battery'    , [arg0]);};
exports.barcode_mode    = function (arg0, success, error) {exec(success, error, 'zb', 'barcode_mode'     , [arg0]);};
exports.barcode_init    = function (arg0, success, error) {exec(success, error, 'zb', 'barcode_init'     , [arg0]);};
exports.barcode_connect = function (arg0, success, error) {exec(success, error, 'zb', 'barcode_connect'  , [arg0]);};
exports.barcode_trigger = function (arg0, success, error) {exec(success, error, 'zb', 'barcode_trigger'  , [arg0]);};
exports.antenna_power   = function (arg0, success, error) {exec(success, error, 'zb', 'antenna_power'    , [arg0]);};
